package org.migor.feedless.http

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.capabilities
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class HttpAuthSupport(
  private val jwtTokenIssuer: JwtTokenIssuer,
  private val groupRepository: GroupRepository,
) {

  suspend fun <T> withUserContext(request: HttpServletRequest, block: suspend (RequestContext) -> T): T {
    val context = resolveRequestContext(request)
    return withContext(context) { block(context) }
  }

  suspend fun resolveRequestContext(request: HttpServletRequest): RequestContext {
    val jwt = try {
      jwtTokenIssuer.decodeJwt(request)
    } catch (_: AccessDeniedException) {
      throw AccessDeniedException("Authentication header required: Bearer <token>")
    }

    val capabilities = jwt.capabilities()
    val userId = capabilities
      .find { it.authority == UserCapability.ID.value }
      ?.let { UserCapability.fromString(it.payload) }
      ?: throw AccessDeniedException("invalid token")

    val groupId = capabilities
      .filter { it.authority == GroupCapability.ID.value }
      .map { GroupCapability.fromString(it.payload).groupId }
      .firstOrNull()
      ?: groupRepository.findAllByOwner(userId).firstOrNull()?.id
      ?: throw BadRequestException("no group for user")

    return RequestContext(userId = userId, groupId = groupId)
  }
}
