package org.migor.rich.rss.auth

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Service
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
  PROVIDE_HTTP_RESPONSE,
  WRITE
}

object JwtParameterNames {
  const val EXP = "exp"
  const val ID = "id"
  const val IAT = "iat"
  const val REMOTE_ADDR = "remote_addr"
  const val USER_ID = "user_id"
  const val TYPE = "token_type"
}

@Service
class AuthService {
  private val log = LoggerFactory.getLogger(AuthService::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  @Value("\${auth.token.anonymous.validForDays}")
  lateinit var tokenAnonymousValidForDays: String

  @Value("\${default.auth.token.anonymous.validForDays}")
  lateinit var defaultTokenAnonymousValidForDays: String

  private var tokenAnonymousValidFor: Long by Delegates.notNull()

  private val attrAuthorities = "authorities"

  @PostConstruct
  fun postConstruct() {
    tokenAnonymousValidFor = parseDuration(tokenAnonymousValidForDays, defaultTokenAnonymousValidForDays)
    log.info("tokenAnonymousValidFor=${tokenAnonymousValidFor}")
  }

  private fun parseDuration(actual: String, fallback: String) = runCatching {
    actual.toLong().toDuration(DurationUnit.DAYS).inWholeMinutes
  }.getOrElse { fallback.toLong() }

  fun getAuthorities(jwt: Jwt): List<String> {
    return jwt.getClaim(attrAuthorities) as List<String>
  }

  fun decodeToken(token: String): OAuth2AuthenticationToken {
    val jwtToken = decodeJwt(token)
    val attributes = mapOf(
      JwtParameterNames.USER_ID to jwtToken.claims[JwtParameterNames.USER_ID]
    )
    val authorities: List<OAuth2UserAuthority> = getAuthorities(jwtToken).map { OAuth2UserAuthority(it, attributes) }
    val nameAttributeKey = JwtParameterNames.USER_ID
    val principal: OAuth2User = DefaultOAuth2User(authorities, attributes, nameAttributeKey)
    val authorizedClientRegistrationId = jwtToken.getClaimAsString("id")
    return OAuth2AuthenticationToken(
      principal,
      authorities,
      authorizedClientRegistrationId
    )
  }

  private fun getSecretKey(): SecretKey {
    return SecretKeySpec(propertyService.jwtSecret.encodeToByteArray(), "HmacSHA256")
  }

  private fun decodeJwt(token: String): Jwt {
    return NimbusJwtDecoder
      .withSecretKey(getSecretKey())
      .build()
      .decode(token)
  }

  fun interceptToken(request: HttpServletRequest): OAuth2AuthenticationToken {
    val rawToken = interceptTokenRaw(request)
    return decodeToken(rawToken)
  }

  fun assertToken(request: HttpServletRequest) {
    if (!isWhitelisted(request)) {
      val rawToken = interceptTokenRaw(request)
      decodeToken(rawToken)
    }
  }

  private fun isWhitelisted(request: HttpServletRequest): Boolean {
    val isWhitelisted = listOf("127.0.0.1", "0:0:0:0:0:0:0:1", "localhost").contains(request.remoteHost)
//    log.debug("[${corrId}] isWhitelisted? ${request.remoteHost} -> $isWhitelisted")
    return isWhitelisted
  }

  fun interceptJwt(request: HttpServletRequest): Jwt {
    val rawToken = interceptTokenRaw(request)
    return decodeJwt(rawToken)
  }

  @Throws(AccessDeniedException::class)
  private fun interceptTokenRaw(request: HttpServletRequest): String {
    val authCookie = request.cookies?.firstOrNull { it.name == "TOKEN" }
    if (StringUtils.isNotBlank(authCookie?.value)) {
        // todo validate ip
        return authCookie?.value!!
    }
    val authHeader = request.getHeader("Authentication")
    if (StringUtils.isNotBlank(authHeader)) {
      return authHeader.replaceFirst("Bearer ", "")
    }
    throw AccessDeniedException("token not present")
  }
}

enum class AuthTokenType(val value: String) {
  ANON("ANON"),
  USER("USER"),
  AGENT("AGENT"),
}
