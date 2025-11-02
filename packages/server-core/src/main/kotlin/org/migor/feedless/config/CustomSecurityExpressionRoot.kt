package org.migor.feedless.config

import org.migor.feedless.session.LazyGrantedAuthority
import org.springframework.security.access.expression.SecurityExpressionRoot
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.core.Authentication

class CustomSecurityExpressionRoot(
  authentication: Authentication
) : SecurityExpressionRoot(authentication), MethodSecurityExpressionOperations {

  private var filterObject: Any? = null
  private var returnObject: Any? = null

  /**
   * Checks if the current user has a specific capability.
   * @param capabilityId The ID of the capability to check (e.g., 'USER', 'AGENT', 'READ', 'WRITE')
   * @return true if the capability is present in the SecurityContext, false otherwise
   */
  fun hasCapability(capabilityId: String): Boolean {
    val authorities = authentication.authorities
    return authorities.any { authority ->
      if (authority is LazyGrantedAuthority) {
        authority.authority == capabilityId
      } else {
        false
      }
    }
  }

  /**
   * Checks if the current user has any valid token/authentication.
   * @return true if authentication is present and authenticated, false otherwise
   */
  fun hasToken(): Boolean {
    return authentication.isAuthenticated
  }

  override fun setFilterObject(filterObject: Any?) {
    this.filterObject = filterObject
  }

  override fun getFilterObject(): Any? {
    return filterObject
  }

  override fun setReturnObject(returnObject: Any?) {
    this.returnObject = returnObject
  }

  override fun getReturnObject(): Any? {
    return returnObject
  }

  override fun getThis(): Any {
    return this
  }
}


