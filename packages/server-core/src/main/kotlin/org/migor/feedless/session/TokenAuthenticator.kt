package org.migor.feedless.session

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

/**
 * Both JWT filters authenticate here. An API token lives 356 days, so its group claim counts only while the user still owns that group;
 * the verdict is cached on the request, so one request costs at most one lookup.
 */
@Component
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class TokenAuthenticator(private val userGroupAssignmentRepository: UserGroupAssignmentRepository) {

  private val log = LoggerFactory.getLogger(TokenAuthenticator::class.simpleName)

  fun authenticate(jwt: Jwt, request: HttpServletRequest): OAuth2AuthenticationToken {
    val capabilities = jwt.capabilities()
    val groupClaim = capabilities.firstOrNull { it.authority == GroupCapability.ID.value }
      ?: return jwtToOAuth2AuthenticationToken(jwt, capabilities)

    val userId = capabilities.firstOrNull { it.authority == UserCapability.ID.value }
      ?.let { runCatching { UserCapability.fromString(it.payload) }.getOrNull() }
    val groupId = runCatching { GroupCapability.fromString(groupClaim.payload).groupId }.getOrNull()
    val owned = userId != null && groupId != null && ownsGroup(userId, groupId, request)
    if (!owned) {
      log.debug("token's group claim dropped: user $userId does not own group $groupId")
    }
    return jwtToOAuth2AuthenticationToken(jwt, if (owned) capabilities else capabilities - groupClaim)
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

  companion object {
    private val GROUP_VERDICT_ATTR = "${TokenAuthenticator::class.qualifiedName}.groupVerdict"
  }
}
