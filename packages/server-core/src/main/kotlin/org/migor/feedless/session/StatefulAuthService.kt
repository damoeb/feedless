package org.migor.feedless.session

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.common.PropertyService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.time.LocalDateTime
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.properties.Delegates
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Service
@Profile("${AppProfiles.session} & ${AppLayer.repository} & ${AppLayer.service}")
class StatefulAuthService : AuthService() {
  private lateinit var whitelistedIps: List<String>
  private val log = LoggerFactory.getLogger(StatefulAuthService::class.simpleName)

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var userSecretRepository: UserSecretRepository

  @Value("\${auth.token.anonymous.validForDays}")
  lateinit var tokenAnonymousValidForDays: String

  @Value("\${default.auth.token.anonymous.validForDays}")
  lateinit var defaultTokenAnonymousValidForDays: String

  @Value("\${app.whitelistedHosts}")
  lateinit var whitelistedHostsParam: String

  @Autowired
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

  private var tokenAnonymousValidFor: Long by Delegates.notNull()

  @PostConstruct
  fun postConstruct() {
    tokenAnonymousValidFor = parseDuration(tokenAnonymousValidForDays, defaultTokenAnonymousValidForDays)
    log.info("tokenAnonymousValidFor=${tokenAnonymousValidFor}")

    resolveWhitelistedHosts()
  }

  override suspend fun parseAndVerify(token: String): Jwt {
    return NimbusJwtDecoder
      .withSecretKey(getSecretKey())
      .build()
      .decode(token)
  }

  override suspend fun interceptToken(request: HttpServletRequest): Jwt {
    return parseAndVerify(interceptTokenRaw(request))
  }

  override suspend fun authenticateUser(email: String, secretKey: String): Jwt = withContext(Dispatchers.IO) {
    log.debug("authRoot")
    val root = userRepository.findByEmail(email) ?: throw NotFoundException("user not found")
    if (!root.admin) {
      throw PermissionDeniedException("account is not root")
    }
    userSecretRepository.findBySecretKeyValue(secretKey, email)
      ?: throw IllegalArgumentException("secretKey does not match")

    jwtTokenIssuer.createJwtForCapabilities(listOf(UserCapability(root.id)))
  }

  override suspend fun findUserById(userId: UserId): User? = withContext(Dispatchers.IO) {
    userRepository.findById(userId)
  }

  override suspend fun findBySecretKeyValue(secretKey: String, email: String): UserSecret? =
    withContext(Dispatchers.IO) {
      userSecretRepository.findBySecretKeyValue(secretKey, email)
    }

  override suspend fun updateLastUsed(id: UserSecretId, date: LocalDateTime) = withContext(Dispatchers.IO) {
    userSecretRepository.updateLastUsed(id, date)
  }

  override suspend fun assertToken(request: HttpServletRequest) {
    if (!isWhitelisted(request)) {
      interceptToken(request)
    }
  }

  override fun isWhitelisted(request: HttpServletRequest): Boolean {
//    val isWhitelisted = whitelistedIps.contains(request.remoteHost)
//    log.info("isWhitelisted? ${request.remoteHost} -> $isWhitelisted")
    return whitelistedIps.contains(request.remoteHost)
  }

  // --

  private fun resolveWhitelistedHosts() {
    this.whitelistedIps = whitelistedHostsParam
      .trim()
      .split(" ", ",").mapNotNull {
        try {
          InetAddress.getByName(it.trim()).hostAddress
        } catch (e: Exception) {
          log.warn("Cannot resolve DNS $it: ${e.message}")
          null
        }
      }
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

  private fun getSecretKey(): SecretKey {
    return SecretKeySpec(propertyService.jwtSecret.encodeToByteArray(), "HmacSHA256")
  }
}
