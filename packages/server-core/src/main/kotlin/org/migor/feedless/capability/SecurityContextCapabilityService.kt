package org.migor.feedless.capability

import org.migor.feedless.session.LazyGrantedAuthority
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component("capabilityService")
class SecurityContextCapabilityService : CapabilityService {

  override fun hasCapability(capabilityId: CapabilityId): Boolean {
    val authorities = getAuthentication().authorities
    return authorities.any { authority ->
      if (authority is LazyGrantedAuthority) {
        authority.authority == capabilityId.value
      } else {
        false
      }
    }
  }

  override fun hasToken(): Boolean {
    return getAuthentication().isAuthenticated
  }

  override fun getCapability(capabilityId: CapabilityId): UnresolvedCapability? {
    val authorities = getAuthentication().authorities
    return authorities
      .filterIsInstance<LazyGrantedAuthority>()
      .filter { authority -> authority.authority == capabilityId.value }
      .map { authority -> UnresolvedCapability(CapabilityId(authority.authority), authority.payload) }
      .firstOrNull()
  }

  private fun getAuthentication(): Authentication {
    return SecurityContextHolder
      .getContext().authentication
  }
}
