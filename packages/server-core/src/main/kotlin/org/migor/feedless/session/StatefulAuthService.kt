package org.migor.feedless.session

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.time.Duration
import java.time.LocalDateTime
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Service
@Profile("${AppProfiles.session} & ${AppLayer.repository} & ${AppLayer.service}")
class StatefulAuthService : AuthService() {
  private lateinit var whitelistedIps: List<String>
  private val log = LoggerFactory.getLogger(StatefulAuthService::class.simpleName)

  @Autowired
  private lateinit var sessionProperties: SessionProperties

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var userSecretRepository: UserSecretRepository

  @Autowired
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository

  @Autowired
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

  // Bounds last-used writes to one per secret per interval, however busy the token is.
  private val lastUsedResolution = Duration.ofMinutes(1)

  @PostConstruct
  fun postConstruct() {
    resolveWhitelistedHosts()
  }

  /** Root login only; everyone else uses SSO or magic mail, since a user-secret value is not a password. */
  override suspend fun authenticateUser(email: String, secretKey: String): Jwt = withContext(Dispatchers.IO) {
    log.debug("authRoot")
    val user = userRepository.findByEmail(email) ?: throw NotFoundException("user not found")
    if (!user.admin) {
      throw PermissionDeniedException("account is not root")
    }
    userSecretRepository.findBySecretKeyValue(secretKey, email)
      ?: throw IllegalArgumentException("secretKey does not match")

    val actingGroup = userGroupAssignmentRepository.actingGroupOf(user.id)
    jwtTokenIssuer.createJwtForCapabilities(listOf(UserCapability(user.id), GroupCapability(actingGroup)))
  }

  override suspend fun findUserById(userId: UserId): User? = withContext(Dispatchers.IO) {
    userRepository.findById(userId)
  }

  override suspend fun findBySecretKeyValue(secretKey: String, email: String): UserSecret? =
    withContext(Dispatchers.IO) {
      userSecretRepository.findBySecretKeyValue(secretKey, email)
    }

  override suspend fun updateLastUsed(id: UserSecretId, date: LocalDateTime) {
    try {
      withContext(Dispatchers.IO) {
        userSecretRepository.updateLastUsed(id, date)
      }
    } catch (e: Exception) {
      log.warn("Exception while updating secret key", e)
    }
  }

  override fun useApiSecret(id: UserSecretId, ownerId: UserId, now: LocalDateTime): Boolean {
    if (userSecretRepository.findById(id)?.ownerId != ownerId) {
      return false
    }
    try {
      userSecretRepository.updateLastUsedIfStale(id, now, now.minus(lastUsedResolution))
    } catch (e: Exception) {
      log.warn("Exception while updating secret key", e)
    }
    return true
  }

//  override suspend fun assertToken(request: HttpServletRequest) {
//    if (!isWhitelisted(request)) {
//      jwtTokenIssuer.decodeJwt(request)
//    }
//  }

  override fun isWhitelisted(request: HttpServletRequest): Boolean {
//    val isWhitelisted = whitelistedIps.contains(request.remoteHost)
//    log.info("isWhitelisted? ${request.remoteHost} -> $isWhitelisted")
    return whitelistedIps.contains(request.remoteHost)
  }

  // --

  private fun resolveWhitelistedHosts() {
    this.whitelistedIps = sessionProperties.whitelistedHosts
      .mapNotNull {
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

  private fun getSecretKey(): SecretKey {
    return SecretKeySpec(sessionProperties.jwtSecret.encodeToByteArray(), "HmacSHA256")
  }
}
