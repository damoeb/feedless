package org.migor.feedless.session

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecretId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Both JWT filters authenticate here. An API token lives 356 days, so it counts only while its secret exists and its group claim only while
 * the user still owns that group; both verdicts are cached on the request, so one request costs at most one lookup of each.
 */
@Component
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class TokenAuthenticator(
  private val userGroupAssignmentRepository: UserGroupAssignmentRepository,
  private val authService: AuthService,
) {

  private val log = LoggerFactory.getLogger(TokenAuthenticator::class.simpleName)

  fun authenticate(jwt: Jwt, request: HttpServletRequest): OAuth2AuthenticationToken {
    val capabilities = jwt.capabilities()
    val userId = capabilities.firstOrNull { it.authority == UserCapability.ID.value }
      ?.let { runCatching { UserCapability.fromString(it.payload) }.getOrNull() }
    if (jwt.getClaimAsString(JwtParameterNames.TYPE) == AuthTokenType.API.value && !apiSecretInUse(jwt, userId, request)) {
      throw AccessDeniedException("API token revoked or unknown")
    }

    val groupClaim = capabilities.firstOrNull { it.authority == GroupCapability.ID.value }
      ?: return jwtToOAuth2AuthenticationToken(jwt, capabilities)

    val groupId = runCatching { GroupCapability.fromString(groupClaim.payload).groupId }.getOrNull()
    val owned = userId != null && groupId != null && ownsGroup(userId, groupId, request)
    if (!owned) {
      log.debug("token's group claim dropped: user $userId does not own group $groupId")
    }
    return jwtToOAuth2AuthenticationToken(jwt, if (owned) capabilities else capabilities - groupClaim)
  }

  // A token without the secret claim predates revocation and is refused rather than trusted.
  private fun apiSecretInUse(jwt: Jwt, userId: UserId?, request: HttpServletRequest): Boolean {
    val secretId = jwt.getClaimAsString(JwtParameterNames.SECRET_ID)
      ?.let { runCatching { UserSecretId(it) }.getOrNull() }
    if (secretId == null || userId == null) {
      return false
    }
    val cached = request.getAttribute(SECRET_VERDICT_ATTR) as? SecretVerdict
    if (cached != null && cached.secretId == secretId && cached.userId == userId) {
      return cached.inUse
    }
    val inUse = authService.useApiSecret(secretId, userId, LocalDateTime.now())
    request.setAttribute(SECRET_VERDICT_ATTR, SecretVerdict(secretId, userId, inUse))
    return inUse
  }

  private fun ownsGroup(userId: UserId, groupId: GroupId, request: HttpServletRequest): Boolean {
    val cached = request.getAttribute(GROUP_VERDICT_ATTR) as? GroupVerdict
    if (cached != null && cached.userId == userId && cached.groupId == groupId) {
      return cached.owned
    }
    val owned = userGroupAssignmentRepository.ownsGroup(userId, groupId)
    request.setAttribute(GROUP_VERDICT_ATTR, GroupVerdict(userId, groupId, owned))
    return owned
  }

  private data class GroupVerdict(val userId: UserId, val groupId: GroupId, val owned: Boolean)

  private data class SecretVerdict(val secretId: UserSecretId, val userId: UserId, val inUse: Boolean)

  companion object {
    private val GROUP_VERDICT_ATTR = "${TokenAuthenticator::class.qualifiedName}.groupVerdict"
    private val SECRET_VERDICT_ATTR = "${TokenAuthenticator::class.qualifiedName}.secretVerdict"
  }
}
