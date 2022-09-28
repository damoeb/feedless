package org.migor.rich.rss.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.ApiErrorCode
import org.migor.rich.rss.api.ApiException
import org.migor.rich.rss.api.dto.AuthResponseDto
import org.migor.rich.rss.api.dto.PermanentFeedUrl
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.annotation.PostConstruct
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.properties.Delegates
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object AuthConfig {
  const val tokenCookie = "wt"
  const val tokenParam = "token"
}

@Service
class AuthService {
  private val log = LoggerFactory.getLogger(AuthService::class.simpleName)

  @Value("\${app.auth:#{null}}")
  lateinit var authMethod: Optional<String>

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Value("\${app.auth.secret}")
  lateinit var authSecret: String

  @Value("\${auth.token.anonymous.validForDays}")
  lateinit var tokenAnonymousValidForDays: String

  @Value("\${default.auth.token.anonymous.validForDays}")
  lateinit var defaultTokenAnonymousValidForDays: String

  private var tokenAnonymousValidFor: Long by Delegates.notNull()

  private val maxAgeWebTokenMin: Long = 10

  @PostConstruct
  fun postConstruct() {
    log.info("authMethod: ${authMethod.orElse("none")}")
    tokenAnonymousValidFor = parseExpiry(tokenAnonymousValidForDays, defaultTokenAnonymousValidForDays)
    log.info("tokenAnonymousValidFor=${tokenAnonymousValidFor}")
  }

  private fun parseExpiry(actual: String, fallback: String) = runCatching {
    actual.toLong().toDuration(DurationUnit.DAYS).inWholeMinutes
  }.getOrElse { fallback.toLong() }

  private val attrRemoteAddr = "ip"
  private val attrType = "type"

  private fun createTokenForAnonymous(): String {
    meterRegistry.counter("issue-token", listOf(Tag.of("type", "anonymous"))).increment()
    log.info("signedToken for anonymous")
    return toJWT(
      mapOf(
        attrType to AuthTokenType.ANON.value,
      )
    )
  }

  private fun createTokenForWeb(remoteAddr: String, existingWebToken: Cookie?): String {
    meterRegistry.counter("issue-token", listOf(Tag.of("type", "web"))).increment()
    log.info("signedToken for web remoteAddr=${remoteAddr}")
    Optional.ofNullable(existingWebToken).map { it.value }
    return toJWT(
      mapOf(
        attrType to AuthTokenType.WEB.value,
        attrRemoteAddr to remoteAddr,
      )
    )
  }

  private fun toJWT(payload: Map<String, String>): String {
    val algorithm: Algorithm = Algorithm.HMAC256(authSecret)
    return JWT.create()
      .withPayload(payload)
      .withIssuedAt(Date())
      .sign(algorithm)
  }

  private fun decodeAuthToken(corrId: String, token: String?): AuthToken {
    if (StringUtils.isBlank(token)) {
      log.debug("[${corrId}] token is null or empty")
      throw ApiException(ApiErrorCode.UNAUTHORIZED, "token not provided")
    }
    val decoded = JWT.decode(token)
    val payloadEnc = Base64.getDecoder().decode(decoded.payload).toString(Charsets.UTF_8)
    val payload = JsonUtil.gson.fromJson<Map<String, Any>>(payloadEnc, Map::class.java)

    val type = resolveTokenType(payload[attrType] as String?)!!
    val remoteAddr = payload[attrRemoteAddr] as String?
    return AuthToken(
      remoteAddr = remoteAddr,
      type = type,
      isAnonymous = type == AuthTokenType.ANON,
      isWeb = type == AuthTokenType.WEB,
      issuedAt = decoded.issuedAt
    )
  }

  private fun resolveTokenType(tokenTypeStr: String?): AuthTokenType? {
    return AuthTokenType.values().firstOrNull { it.value == tokenTypeStr }
  }

  fun validateAuthToken(corrId: String, request: HttpServletRequest): AuthToken {
    log.debug("[${corrId}] validateAuthToken1")
    val remoteAddr = request.remoteAddr
    if (isWhitelisted(request)) {
      log.info("[${corrId}] whitelisted")
      return AuthToken(
        remoteAddr = remoteAddr,
        type = AuthTokenType.INTERNAL,
        isAnonymous = false,
        isWeb = false,
        issuedAt = Date()
      )

    } else {
      val token = interceptToken(request)
      return validateAuthToken(corrId, token, remoteAddr)
    }
  }

  fun validateAuthToken(corrId: String, token: String?, remoteAddr: String): AuthToken {
    log.debug("[${corrId}] validateAuthToken2")
    if (StringUtils.isBlank(token)) {
      log.debug("[${corrId}] token is null or empty")
      throw ApiException(ApiErrorCode.UNAUTHORIZED, "token not provided")
    }
    val payload = decodeAuthToken(corrId, token)

    if (payload.isWeb) {
//      if (payload.remoteAddr != remoteAddr) {
//        throw ApiException(ApiErrorCode.UNAUTHORIZED, "ip mismatch expected ${payload.remoteAddr} found ${remoteAddr}")
//      }
      val validForMin = maxAgeWebTokenMin
      val issuedAt = payload.issuedAt.toInstant()
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
    }

    log.info("[${corrId}] auth type=${payload.type} iat=${payload.issuedAt}")
    return payload
  }

  fun requestStandaloneFeedUrl(corrId: String, feedUrl: String, request: HttpServletRequest): PermanentFeedUrl {
    val token = createTokenForAnonymous()
    val host = if(feedUrl.startsWith("http")) {
      ""
    } else {
      propertyService.publicUrl
    }
    return PermanentFeedUrl(
      feedUrl = "${host}${feedUrl}&${AuthConfig.tokenParam}=${URLEncoder.encode(token, StandardCharsets.UTF_8)}",
      type = AuthTokenType.ANON
    )
  }

  fun authForWeb(): AuthResponseDto {
    val maxAge = maxAgeWebTokenMin * 60
    return AuthResponseDto(
      maxAge = maxAge.toInt()
    )
  }

  fun issueWebToken(request: HttpServletRequest, response: HttpServletResponse) {
    val existingToken = getCookiesByName(AuthConfig.tokenCookie, request)
    val sessionToken = createTokenForWeb(request.remoteAddr, existingToken?.firstOrNull())
    val sessionCookie = Cookie(AuthConfig.tokenCookie, sessionToken)
    sessionCookie.isHttpOnly = true
    val maxAge = maxAgeWebTokenMin * 60
    sessionCookie.maxAge = maxAge.toInt()
    response.addCookie(sessionCookie)
  }

  fun interceptToken(request: HttpServletRequest): String {
    val tokenFromParams = request.getParameter(AuthConfig.tokenParam)
    return if (StringUtils.isBlank(tokenFromParams)) {
      if (isWhitelisted(request)) {
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

  private fun isWhitelisted(request: HttpServletRequest): Boolean {
    log.info("isWhitelisted? ${request.remoteHost}")
    return arrayOf("127.0.0.1", "localhost").contains(request.remoteHost)
  }

  fun getCookiesByName(name: String, request: HttpServletRequest) =
    request.cookies?.filter { it.name == AuthConfig.tokenCookie }
}

enum class AuthTokenType(val value: String) {
  WEB("web"),
  ANON("anonymous"),
  INTERNAL("internal"),
  LEGACY("legacy"),
}

data class AuthToken(
  val type: AuthTokenType,
  val remoteAddr: String?,
  val isAnonymous: Boolean,
  val isWeb: Boolean,
  val issuedAt: Date
)
