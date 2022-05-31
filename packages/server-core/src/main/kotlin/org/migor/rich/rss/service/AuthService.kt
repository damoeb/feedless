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
    actual.toLong()
  }.getOrElse { fallback.toLong() }


  fun createAuthToken(email: String?): AuthResponseDto {
    return AuthResponseDto(
      token = Optional.ofNullable(email)
      .map { createTokenForUser(it) }
      .orElse(createTokenForAnonymous()))
  }

  private val payloadAttributeUser = "user"

  private fun createTokenForAnonymous(): String {
    val tokenPayload = mapOf(
      payloadAttributeUser to ""
    )

    return signToken(tokenPayload)
  }

  private fun createTokenForUser(email: String): String {
    val tokenPayload = mapOf(
      payloadAttributeUser to email
    )

    return signToken(tokenPayload)
  }

  private fun signToken(payload: Map<String, String>): String {
    val algorithm: Algorithm = Algorithm.HMAC256(authSecret)
    return JWT.create()
      .withPayload(payload)
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

    val user = payload[payloadAttributeUser] as String
    return AuthToken(user=user, isAnonymous=StringUtils.isBlank(user), issuedAt=decoded.issuedAt)
  }

  fun validateAuthToken(corrId: String, token: String?) {
    if (StringUtils.isBlank(token)) {
      log.debug("[${corrId}] token is null or empty")
      throw RuntimeException("token not provided")
    }
    val payload = decodeAuthToken(corrId, token)

    val validFor = if (payload.isAnonymous) {
      tokenAnonymousValidFor
    } else {
      tokenUserValidFor
    }

    val issuedAt = payload.issuedAt.toInstant()
      .atZone(ZoneId.systemDefault())
      .toLocalDateTime()
    val expiry = issuedAt
      .plusDays(validFor)
    log.debug("[${corrId}] issuedAt ${issuedAt}")
    if (expiry
      .isBefore(LocalDateTime.now())) {
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

data class AuthToken(val user: String, val isAnonymous: Boolean, val issuedAt: Date) {

}
