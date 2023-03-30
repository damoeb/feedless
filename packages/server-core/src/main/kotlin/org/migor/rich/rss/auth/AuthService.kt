package org.migor.rich.rss.auth

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.ApiErrorCode
import org.migor.rich.rss.api.ApiException
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

object JwtParameterNames {
  const val EXP = "exp"
  const val ID = "id"
  const val IAT = "iat"
  const val REMOTE_ADDR = "remote_addr"
  const val USER_ID = "user_id"
  const val TYPE = "type"
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
    log.debug("[${corrId}] isWhitelisted? ${request.remoteHost} -> $isWhitelisted")
    return isWhitelisted
  }

  fun getCookiesByName(name: String, request: HttpServletRequest) =
    request.cookies?.filter { it.name == AuthConfig.tokenCookie }

  fun getAuthorities(jwt: Jwt): List<String> {
    return jwt.getClaim(attrAuthorities) as List<String>
  }

  fun decodeToken(corrId: String, token: String): OAuth2AuthenticationToken {
    val jwtToken = decodeJwt(token)

//    if (isWhitelisted(corrId, request)) {
//      return AuthToken(
//        remoteAddr = remoteAddr,
//        type = AuthTokenType.INTERNAL,
//        isAnonymous = false,
//      )
//
//    } else {
//      val token = interceptToken(corrId, request)
//      return decodeAuthToken2(corrId, token)
//    }

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

  @Throws(AccessDeniedException::class)
  fun interceptTokenCookie(request: HttpServletRequest): OAuth2AuthenticationToken {
      val authCookie = request.cookies?.firstOrNull { it.name == "TOKEN" }
      if (StringUtils.isNotBlank(authCookie?.value)) {
        // todo validate ip
        return decodeToken("-", authCookie?.value!!)
      }
      throw AccessDeniedException("token not present")
  }
}

enum class AuthTokenType(val value: String) {
  ANON("anonymous"),
  USER("user"),
  INTERNAL("internal"),
}
