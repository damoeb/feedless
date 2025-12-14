package org.migor.feedless.session

import org.migor.feedless.capability.UserCapability
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import kotlin.coroutines.CoroutineContext

private fun OAuth2AuthenticationToken.getUserCapability(): UserCapability? {
  return this.authorities.find { it.authority == UserCapability.ID.value }
    ?.let { UserCapability(UserCapability.fromString((it as LazyGrantedAuthority).getPayload())) }
}

fun createRequestContext(): RequestContext {
  val context = RequestContext(corrId = newCorrId())

  runCatching {
    context.userId = if (SecurityContextHolder.getContext().authentication is OAuth2AuthenticationToken) {
      (SecurityContextHolder.getContext().authentication as OAuth2AuthenticationToken).getUserCapability()?.userId
    } else {
      null
    }

//    context.product = runCatching {
//      RequestContextHolder.currentRequestAttributes().getAttribute("product", RequestAttributes.SCOPE_REQUEST)
//        ?.let {
//          Vertical.valueOf(it as String)
//        }
//    }.getOrNull()

  }.onFailure {
    println(it.message)
  }
  return context
}

data class RequestContext(
  val corrId: String? = newCorrId(),
  val isAdmin: Boolean? = false,
  var userId: UserId? = null,
  var groupId: GroupId? = null
) : CoroutineContext.Element {
  companion object Key : CoroutineContext.Key<RequestContext>

  override val key: CoroutineContext.Key<*> = Key
}
