package org.migor.feedless.capability

import org.migor.feedless.session.LazyGrantedAuthority
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component("capabilityService")
class SecurityContextCapabilityService : CapabilityService {

  override fun hasCapability(capabilityId: String): Boolean {
    val authorities = getAuthentication().authorities
    return authorities.any { authority ->
      if (authority is LazyGrantedAuthority) {
        authority.authority == capabilityId
      } else {
        false
      }
    }
  }

  override fun hasToken(): Boolean {
    return getAuthentication().isAuthenticated
  }

  private fun getAuthentication(): Authentication {
    return SecurityContextHolder
      .getContext().authentication
  }
}
