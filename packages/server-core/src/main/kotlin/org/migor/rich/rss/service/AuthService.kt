package org.migor.rich.rss.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.AuthResponseDto
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.annotation.PostConstruct
import kotlin.properties.Delegates
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
class AuthService {
  private val log = LoggerFactory.getLogger(AuthService::class.simpleName)

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


  fun createAuthToken(csrf: String?, email: String?): AuthResponseDto {
    val token = Optional.ofNullable(csrf).map { createTokenForWeb() }.orElse(Optional.ofNullable(email)
      .map { createTokenForUser(it) }
      .orElse(createTokenForAnonymous()))

    return AuthResponseDto(
      token = token
    )
  }

  private val attrUser = "userId"
  private val attrType = "type"
  private val typePersonal = "personal"
  private val typeWeb = "wev"
  private val typeAnon = "anonymous"

  private fun createTokenForAnonymous(): String = signedToken(type = typeAnon)
  private fun createTokenForWeb(): String = signedToken(type = typeWeb)

  private fun createTokenForUser(email: String): String = signedToken(type = typePersonal, userId = email)

  private fun signedToken(type: String, userId: String? = null): String {
    val algorithm: Algorithm = Algorithm.HMAC256(authSecret)
    return JWT.create()
      .withPayload(
        mapOf(
          attrType to type,
          attrUser to userId
        )
      )
      .withIssuedAt(Date())
      .sign(algorithm)
  }

  fun decodeAuthToken(corrId: String, token: String?): AuthToken {
    if (StringUtils.isBlank(token)) {
      log.debug("[${corrId}] token is null or empty")
      throw RuntimeException("token not provided")
    }
    val decoded = JWT.decode(token)
    val payloadEnc = Base64.getDecoder().decode(decoded.payload).toString(Charsets.UTF_8)
    val payload = JsonUtil.gson.fromJson<Map<String, Any>>(payloadEnc, Map::class.java)

    val user = payload.getOrDefault(attrUser, "") as String
    val type = payload[attrType] as String
    return AuthToken(
      user = user,
      isAnonymous = type == typeAnon,
      isWeb = type == typeWeb,
      isPersonal = type == typePersonal,
      issuedAt = decoded.issuedAt
    )
  }

  fun validateAuthToken(corrId: String, token: String?) {
    if (StringUtils.isBlank(token)) {
      log.debug("[${corrId}] token is null or empty")
      throw RuntimeException("token not provided")
    }
    val payload = decodeAuthToken(corrId, token)

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
      throw RuntimeException("Token is outdated")
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
  }

}

data class AuthToken(
  val user: String,
  val isAnonymous: Boolean,
  val isWeb: Boolean,
  val isPersonal: Boolean,
  val issuedAt: Date
) {

}
