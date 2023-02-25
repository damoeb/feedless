package org.migor.rich.rss.service

import com.nimbusds.jose.jwk.source.ImmutableSecret
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.ApiErrorCode
import org.migor.rich.rss.api.ApiException
import org.migor.rich.rss.api.ApiParams
import org.migor.rich.rss.api.ApiUrls
import org.migor.rich.rss.api.dto.PermanentFeedUrl
import org.migor.rich.rss.auth.InMemoryOauthRequestRepository
import org.migor.rich.rss.data.jpa.models.OneTimePasswordEntity
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.repositories.OneTimePasswordDAO
import org.migor.rich.rss.data.jpa.repositories.UserDAO
import org.migor.rich.rss.generated.types.Authentication
import org.migor.rich.rss.mail.MailService
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.properties.Delegates
import kotlin.time.DurationUnit
import kotlin.time.toDuration


object AuthConfig {
  const val tokenCookie = "wt"
  const val tokenParam = "token"
}

enum class Authority {
  READ,
  WRITE
}

@Service
class AuthService {
  private val log = LoggerFactory.getLogger(AuthService::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var mailService: MailService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var oneTimePasswordDAO: OneTimePasswordDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var inMemoryOauthRequestRepository: InMemoryOauthRequestRepository

  @Value("\${auth.token.anonymous.validForDays}")
  lateinit var tokenAnonymousValidForDays: String

  @Value("\${default.auth.token.anonymous.validForDays}")
  lateinit var defaultTokenAnonymousValidForDays: String

  private var tokenAnonymousValidFor: Long by Delegates.notNull()

  private val maxAgeWebTokenMin: Long = 10

  @PostConstruct
  fun postConstruct() {
    tokenAnonymousValidFor = parseExpiry(tokenAnonymousValidForDays, defaultTokenAnonymousValidForDays)
    log.info("tokenAnonymousValidFor=${tokenAnonymousValidFor}")
  }

  private fun parseExpiry(actual: String, fallback: String) = runCatching {
    actual.toLong().toDuration(DurationUnit.DAYS).inWholeMinutes
  }.getOrElse { fallback.toLong() }

  private val attrRemoteAddr = "ip"
  private val attrType = "type"
  private val attrAuthorities = "authorities"

  private fun createJwtForAnonymous(): Jwt {
    meterRegistry.counter("issue-token", listOf(Tag.of("type", "anonymous"))).increment()
    log.info("signedToken for anonymous")
    return encodeJwt(
      mapOf(
        attrType to AuthTokenType.ANON.value,
        attrAuthorities to toAuthorities(listOf(Authority.READ)),
      )
    )
  }

  fun createTokenForUser(user: UserEntity): Jwt {
    meterRegistry.counter("issue-token", listOf(Tag.of("type", "user"))).increment()
    log.info("signedToken for user")
    return encodeJwt(
      mapOf(
        attrType to AuthTokenType.USER.value,
        "id" to user.id.toString(),
        attrAuthorities to toAuthorities(listOf(Authority.READ, Authority.WRITE)),
      )
    )
  }

  private fun toAuthorities(authorities: List<Authority>): List<String> {
    return authorities.map { it.name }
  }

  private fun decodeAuthToken(corrId: String, token: String?): AuthToken {
    if (StringUtils.isBlank(token)) {
      log.debug("[${corrId}] token is null or empty")
      throw ApiException(ApiErrorCode.UNAUTHORIZED, "token not provided")
    }
    val decoded = decodeJwt(token!!)
    val claims = decoded.claims

    val type = resolveTokenType(claims[attrType] as String?)!!
    val remoteAddr = claims[attrRemoteAddr] as String?
    return AuthToken(
      remoteAddr = remoteAddr,
      type = type,
      isAnonymous = type == AuthTokenType.ANON,
      issuedAt = decoded.issuedAt!!
    )
  }

  private fun resolveTokenType(tokenTypeStr: String?): AuthTokenType? {
    return AuthTokenType.values().firstOrNull { it.value == tokenTypeStr }
  }

  fun validateAuthToken(corrId: String, request: HttpServletRequest): AuthToken {
    log.debug("[${corrId}] validateAuthToken1")
    val remoteAddr = request.remoteAddr
    if (isWhitelisted(corrId, request)) {
      return AuthToken(
        remoteAddr = remoteAddr,
        type = AuthTokenType.INTERNAL,
        isAnonymous = false,
        issuedAt = Date().toInstant()
      )

    } else {
      val token = interceptToken(corrId, request)
      return validateAuthToken(corrId, token)
    }
  }

  fun validateAuthToken(corrId: String, token: String?): AuthToken {
    log.debug("[${corrId}] validateAuthToken2")
    if (StringUtils.isBlank(token)) {
      log.debug("[${corrId}] token is null or empty")
      throw ApiException(ApiErrorCode.UNAUTHORIZED, "token not provided")
    }
    val payload = decodeAuthToken(corrId, token)

//      if (payload.remoteAddr != remoteAddr) {
//        throw ApiException(ApiErrorCode.UNAUTHORIZED, "ip mismatch expected ${payload.remoteAddr} found ${remoteAddr}")
//      }
    val validForMin = maxAgeWebTokenMin
    val issuedAt = payload.issuedAt
      .atZone(ZoneId.systemDefault())
      .toLocalDateTime()
    val expiry = issuedAt
      .plusMinutes(validForMin)
    log.debug("[${corrId}] issuedAt $issuedAt")
    if (expiry
        .isBefore(LocalDateTime.now())
    ) {
      log.warn("[${corrId}] outdated")
      throw ApiException(ApiErrorCode.UNAUTHORIZED, "Token is outdated")
    }

    log.info("[${corrId}] auth type=${payload.type} iat=${payload.issuedAt}")
    return payload
  }

  fun requestStandaloneFeedUrl(corrId: String, feedUrl: String, request: HttpServletRequest): PermanentFeedUrl {
    val jwt = createJwtForAnonymous()
    val host = if (feedUrl.startsWith("http")) {
      ""
    } else {
      propertyService.publicUrl
    }
    return PermanentFeedUrl(
      feedUrl = "${host}${feedUrl}&${AuthConfig.tokenParam}=${
        URLEncoder.encode(
          jwt.tokenValue,
          StandardCharsets.UTF_8
        )
      }",
      type = AuthTokenType.ANON
    )
  }

  fun interceptToken(corrId: String, request: HttpServletRequest): String {
    val tokenFromParams = request.getParameter(AuthConfig.tokenParam)
    return if (StringUtils.isBlank(tokenFromParams)) {
      if (isWhitelisted(corrId, request)) {
        ""
      } else {
        Optional.ofNullable(
          getCookiesByName(AuthConfig.tokenCookie, request)?.map { it.value }?.firstOrNull()
        ).orElseThrow { ApiException(ApiErrorCode.UNAUTHORIZED, "token not found") }
      }
    } else {
      tokenFromParams
    }
  }

  private fun isWhitelisted(corrId: String, request: HttpServletRequest): Boolean {
    val isWhitelisted = listOf("127.0.0.1", "0:0:0:0:0:0:0:1", "localhost").contains(request.remoteHost)
    log.info("[${corrId}] isWhitelisted? ${request.remoteHost} -> $isWhitelisted")
    return isWhitelisted
  }

  fun getCookiesByName(name: String, request: HttpServletRequest) =
    request.cookies?.filter { it.name == AuthConfig.tokenCookie }

  fun initiateUserSession(corrId: String, email: String): Publisher<Authentication> {
    return Flux.create { emitter ->
      userDAO.findByEmail(email).ifPresent {
        val otp = OneTimePasswordEntity()
        otp.password = UUID.randomUUID().toString()
        otp.validUntil = Timestamp.valueOf(LocalDateTime.now().plusMinutes(5))
        inMemoryOauthRequestRepository.store(oneTimePasswordDAO.save(otp), emitter)
        val subject = "Authorize Access"
        val text = "${propertyService.publicUrl}${ApiUrls.magicMail}?${ApiParams.nonce}=${otp.password}"
        mailService.send(it.email, subject, text)
      }
    }
  }

  fun authorizeViaMail(nonce: String) {
    val otp = oneTimePasswordDAO.findByPassword(nonce)
    if (otp.isPresent) {
      oneTimePasswordDAO.delete(otp.get())
      // validate otp
      val sink = inMemoryOauthRequestRepository.pop(otp.get())
      sink?.next(
        Authentication.newBuilder()
          .token("foo-token")
          .authorities(emptyList())
          .build()
      )
    }
  }

  fun initiateAnonymousSession(): Authentication {
    val jwt = createJwtForAnonymous()
    return Authentication.newBuilder()
      .token(jwt.tokenValue)
      .authorities(getAuthorities(jwt))
      .build()
  }

  fun getAuthorities(jwt: Jwt): List<String> {
    return jwt.getClaim(attrAuthorities) as List<String>
  }

  private fun getSecretKey(): SecretKey {
    return SecretKeySpec(propertyService.jwtSecret.encodeToByteArray(), "HmacSHA256")
  }

  fun encodeJwt(claims: Map<String, Any>, expiresIn: Long = 10): Jwt {
    // https://en.wikipedia.org/wiki/JSON_Web_Token
    val jwsHeader = JwsHeader.with { "HS256" }.build()
    val claimsSet = JwtClaimsSet.builder()
      .issuer(propertyService.publicUrl)
      .claims { c -> c.putAll(claims) }
      .claims { c -> c["id"] = "rich" }
      .claims { c -> c["iat"] = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond() }
      .claims { c ->
        c["exp"] = LocalDateTime.now().plusMinutes(expiresIn).atZone(ZoneId.systemDefault()).toEpochSecond()
      }
      .build()
    val params = JwtEncoderParameters.from(jwsHeader, claimsSet)
    return NimbusJwtEncoder(ImmutableSecret(getSecretKey()))
      .encode(params)
  }

  fun decodeJwt(token: String): Jwt {
    return NimbusJwtDecoder
      .withSecretKey(getSecretKey())
      .build()
      .decode(token)
  }

}

enum class AuthTokenType(val value: String) {
  ANON("anonymous"),
  USER("user"),
  INTERNAL("internal"),
}

data class AuthToken(
  val type: AuthTokenType,
  val remoteAddr: String?,
  val isAnonymous: Boolean,
  val issuedAt: Instant
)
