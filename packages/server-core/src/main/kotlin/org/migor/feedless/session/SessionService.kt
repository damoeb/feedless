package org.migor.feedless.session

import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.util.CryptUtil.newCorrId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

private fun OAuth2AuthenticationToken.getUserCapability(): UserCapability? {
  return this.authorities.find { it.authority == UserCapability.ID.value }
    ?.let { UserCapability(UserCapability.fromString((it as LazyGrantedAuthority).getPayload())) }
}

private fun OAuth2AuthenticationToken.getGroupCapability(): GroupCapability? {
  return this.authorities.find { it.authority == GroupCapability.ID.value }
    ?.let { GroupCapability(GroupCapability.fromString((it as LazyGrantedAuthority).getPayload())) }
}

fun createRequestContext(): RequestContext {
  val context = RequestContext(corrId = newCorrId())

  runCatching {
    context.userId = if (SecurityContextHolder.getContext().authentication is OAuth2AuthenticationToken) {
      (SecurityContextHolder.getContext().authentication as OAuth2AuthenticationToken).getUserCapability()?.userId
    } else {
      null
    }
    context.groupId = if (SecurityContextHolder.getContext().authentication is OAuth2AuthenticationToken) {
      val groupCapability: GroupCapability? =
        (SecurityContextHolder.getContext().authentication as OAuth2AuthenticationToken).getGroupCapability()
      groupCapability?.group?.groupId
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
