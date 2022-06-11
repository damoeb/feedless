package org.migor.rich.rss.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.ApiErrorCode
import org.migor.rich.rss.api.ApiException
import org.migor.rich.rss.api.dto.PermanentFeedUrl
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.stereotype.Service
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

@Service
class AuthService {
  private val log = LoggerFactory.getLogger(AuthService::class.simpleName)

  @Autowired
  lateinit var tokenRepository: CookieCsrfTokenRepository

  @Value("\${app.auth:#{null}}")
  lateinit var authMethod: Optional<String>

  @Value("\${app.auth.secret}")
  lateinit var authSecret: String

  @Value("\${auth.token.anonymous.validForDays}")
  lateinit var tokenAnonymousValidForDays: String

  @Value("\${default.auth.token.anonymous.validForDays}")
  lateinit var defaultTokenAnonymousValidForDays: String

  private var tokenAnonymousValidFor: Long by Delegates.notNull()

  @Value("\${auth.token.user.validForDays}")
  lateinit var tokenUserValidForDays: String

  @Value("\${auth.enforced}")
  lateinit var tokenEnforced: String

  @Value("\${default.auth.token.user.validForDays}")
  lateinit var defaultTokenUserValidForDays: String

  private var tokenUserValidFor: Long by Delegates.notNull()

  @PostConstruct
  fun postConstruct() {
    log.info("authMethod: ${authMethod.orElse("none")}")
    tokenAnonymousValidFor = parseExpiry(tokenAnonymousValidForDays, defaultTokenAnonymousValidForDays)
    log.info("tokenAnonymousValidFor=${tokenAnonymousValidFor}")
    tokenUserValidFor = parseExpiry(tokenUserValidForDays, defaultTokenUserValidForDays)
    log.info("tokenUserValidFor=${tokenUserValidFor}")
  }

  private fun parseExpiry(actual: String, fallback: String) = runCatching {
    actual.toLong().toDuration(DurationUnit.DAYS).inWholeHours
  }.getOrElse { fallback.toLong() }

  private val attrUser = "userId"
  private val attrRemoteAddr = "ip"
  private val attrType = "type"
  private val typePersonal = "personal"
  private val typeWeb = "web"
  private val typeAnon = "anonymous"

  private fun createTokenForAnonymous(): String = signedToken(type = typeAnon)
  fun createTokenForWeb(remoteAddr: String): String = signedToken(type = typeWeb, remoteAddr = remoteAddr)

  private fun createTokenForUser(email: String): String = signedToken(type = typePersonal, userId = email)

  private fun signedToken(type: String, userId: String? = null, remoteAddr: String? = null): String {
    log.info("signedToken type=${type} remoteAddr=${remoteAddr}")
    val algorithm: Algorithm = Algorithm.HMAC256(authSecret)
    return JWT.create()
      .withPayload(
        mapOf(
          attrType to type,
          attrUser to userId,
          attrRemoteAddr to remoteAddr
        ).filterNot { StringUtils.isBlank(it.value) }
      )
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

    val user = payload.getOrDefault(attrUser, "") as String
    val type = payload[attrType] as String
    val remoteAddr = payload[attrRemoteAddr] as String?
    return AuthToken(
      user = user,
      remoteAddr = remoteAddr,
      type = type,
      isAnonymous = type == typeAnon,
      isWeb = type == typeWeb,
      isPersonal = type == typePersonal,
      issuedAt = decoded.issuedAt
    )
  }

  fun validateAuthToken(corrId: String, token: String?, remoteAddr: String): AuthToken {
    log.debug("[${corrId}] validateAuthToken")
    if (StringUtils.isBlank(token)) {
      log.debug("[${corrId}] token is null or empty")
      throw ApiException(ApiErrorCode.UNAUTHORIZED, "token not provided")
    }
    val payload = decodeAuthToken(corrId, token)

    if (payload.isWeb && payload.remoteAddr != remoteAddr) {
      throw ApiException(ApiErrorCode.UNAUTHORIZED, "ip mismatch expected ${payload.remoteAddr} found ${remoteAddr}")
    }

    val validForHours = if (payload.isAnonymous) {
      tokenAnonymousValidFor
    } else if (payload.isWeb) {
      1 // hour
    } else {
      tokenUserValidFor
    }

    val issuedAt = payload.issuedAt.toInstant()
      .atZone(ZoneId.systemDefault())
      .toLocalDateTime()
    val expiry = issuedAt
      .plusHours(validForHours)
    log.debug("[${corrId}] issuedAt ${issuedAt}")
    if (expiry
        .isBefore(LocalDateTime.now())
    ) {
      log.warn("[${corrId}] outdated")
      if (tokenEnforced.lowercase() == "true") {
        throw ApiException(ApiErrorCode.UNAUTHORIZED, "Token is outdated")
      }
    } else {
      if (payload.isAnonymous) {
        if (expiry
            .plusWeeks(1)
            .isBefore(LocalDateTime.now())
        ) {
          log.warn("[${corrId}] will expire in two weeks")
        }
      } else {
        if (expiry
            .plusWeeks(8)
            .isBefore(LocalDateTime.now())
        ) {
          log.warn("[${corrId}] will expire in two weeks")
        }
      }
    }
    log.info("[${corrId}] auth type=${payload.type} iat=${payload.issuedAt}")
    return payload
  }

  fun requestPermaFeedUrl(corrId: String, feedUrl: String, request: HttpServletRequest, response: HttpServletResponse): PermanentFeedUrl {
    val cookieName = "REMEMBER_RICH_TOKEN"
    val tokenFromCookie = request.cookies.filter { it.name == cookieName }.map { it.value }.firstOrNull()
    val token = if (StringUtils.isBlank(tokenFromCookie)) {
      log.info("[${corrId}] claiming new token for anon")
      createTokenForAnonymous()
    } else {
      log.info("[${corrId}] reusing token from cookie")
      tokenFromCookie
    }

    val cookie = Cookie(cookieName, token)
    cookie.isHttpOnly = true
    cookie.path = "/"
    response.addCookie(cookie)

    return PermanentFeedUrl(
      feedUrl ="${feedUrl}&token=${token}",
      type = typeAnon
    )
  }

  fun injectCsrfCookie(request: HttpServletRequest) {
    tokenRepository.generateToken(request)
  }

}

data class AuthToken(
  val user: String,
  val type: String,
  val remoteAddr: String?,
  val isAnonymous: Boolean,
  val isWeb: Boolean,
  val isPersonal: Boolean,
  val issuedAt: Date
)
