package org.migor.feedless.session

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.migor.feedless.user.UserDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.properties.Delegates
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Service
@Profile(AppProfiles.database)
class AuthService : IAuthService {
  private lateinit var whitelistedIps: List<String>
  private val log = LoggerFactory.getLogger(AuthService::class.simpleName)

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var userDAO: UserDAO

  @Value("\${auth.token.anonymous.validForDays}")
  lateinit var tokenAnonymousValidForDays: String

  @Value("\${default.auth.token.anonymous.validForDays}")
  lateinit var defaultTokenAnonymousValidForDays: String

  @Value("\${app.whitelistedHosts}")
  lateinit var whitelistedHostsParam: String

  private var tokenAnonymousValidFor: Long by Delegates.notNull()

  private val attrAuthorities = "authorities"

  @PostConstruct
  fun postConstruct() {
    tokenAnonymousValidFor = parseDuration(tokenAnonymousValidForDays, defaultTokenAnonymousValidForDays)
    log.info("tokenAnonymousValidFor=${tokenAnonymousValidFor}")

    resolveWhitelistedHosts()
  }

  private fun resolveWhitelistedHosts() {
    this.whitelistedIps = whitelistedHostsParam
      .trim()
      .split(" ", ",")
      .map {
        try {
          InetAddress.getByName(it.trim()).hostAddress
        } catch (e: Exception) {
          log.warn("Cannot resolve DNS $it: ${e.message}")
          null
        }
      }
      .filterNotNull()
      .plus(
        listOf(
          InetAddress.getLocalHost().hostAddress,
          InetAddress.getLoopbackAddress().hostAddress,
          "127.0.0.1",
          "0:0:0:0:0:0:0:1"
        )
      )
      .distinct()
    log.info("whitelistedIps=${whitelistedIps}")
  }

  private fun parseDuration(actual: String, fallback: String) = runCatching {
    actual.toLong().toDuration(DurationUnit.DAYS).inWholeMinutes
  }.getOrElse { fallback.toLong() }

  fun getAuthorities(jwt: Jwt): List<String> {
    return jwt.getClaim(attrAuthorities) as List<String>
  }

  override fun decodeToken(token: String): OAuth2AuthenticationToken {
    val jwtToken = decodeJwt(token)
    val userId = jwtToken.claims[JwtParameterNames.USER_ID] as String
    val attributes = mapOf(
      JwtParameterNames.USER_ID to userId
    )
    if (StringUtils.isNotBlank(userId) && !userDAO.existsById(UUID.fromString(userId))) {
      throw AccessDeniedException("user does not exist")
    }
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

  override fun interceptJwt(request: HttpServletRequest): Jwt {
    val rawToken = interceptTokenRaw(request)
    return decodeJwt(rawToken)
  }

  override fun assertToken(request: HttpServletRequest) {
    if (!isWhitelisted(request)) {
      val rawToken = interceptTokenRaw(request)
      decodeToken(rawToken)
    }
  }

  fun isWhitelisted(request: HttpServletRequest): Boolean {
//    val isWhitelisted = whitelistedIps.contains(request.remoteHost)
//    log.info("isWhitelisted? ${request.remoteHost} -> $isWhitelisted")
    return whitelistedIps.contains(request.remoteHost)
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
