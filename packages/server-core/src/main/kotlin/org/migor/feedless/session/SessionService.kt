package org.migor.feedless.session

import kotlinx.coroutines.currentCoroutineContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private fun OAuth2AuthenticationToken.getUserCapability(): UserCapability? {
  return this.authorities.find { it.authority == UserCapability.ID.value }
    ?.let { UserCapability(UserCapability.fromString((it as LazyGrantedAuthority).getPayload())) }
}

fun createRequestContext(): RequestContext {
  val context = RequestContext(corrId = newCorrId())

  runCatching {
//    context.userId = if (SecurityContextHolder.getContext().authentication is OAuth2AuthenticationToken) {
//      (SecurityContextHolder.getContext().authentication as OAuth2AuthenticationToken).getUserCapability()?.userId
//    } else {
//      null
//    }

    context.product = runCatching {
      RequestContextHolder.currentRequestAttributes().getAttribute("product", RequestAttributes.SCOPE_REQUEST)
        ?.let {
          Vertical.valueOf(it as String)
        }
    }.getOrNull()

  }.onFailure {
    println(it.message)
  }
  return context
}

class RequestContext(
  var product: Vertical? = null,
  val corrId: String? = newCorrId(),
  var userId: UserId? = null
) : AbstractCoroutineContextElement(RequestContext) {
  companion object Key : CoroutineContext.Key<RequestContext>
}

@Service
@Transactional(propagation = Propagation.NEVER)
@Deprecated("Use capabilityservice instead")
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class SessionService {

  @Autowired
  private lateinit var authService: AuthService

  @Autowired
  private lateinit var capabilityService: CapabilityService

  suspend fun isUser(): Boolean {
    val userCapability = capabilityService.getCapability(UserCapability.ID)
    return userCapability != null
  }

  @Transactional(readOnly = true)
  suspend fun user(): User {
    val notFoundException = IllegalArgumentException("user not found")
    return capabilityService.getCapability(UserCapability.ID)
      ?.let { authService.findUserById(UserCapability.resolve(it)) ?: throw notFoundException }
      ?: throw notFoundException
  }

  suspend fun activeProductFromRequest(): Vertical? {
    return currentCoroutineContext()[RequestContext]?.product
  }

  @Deprecated("")
  suspend fun userId(): UserId? = capabilityService.getCapability(UserCapability.ID)
    ?.let { UserCapability.resolve(it) }

}
