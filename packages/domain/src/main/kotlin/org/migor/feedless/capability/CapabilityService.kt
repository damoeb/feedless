package org.migor.feedless.capability

interface CapabilityService {

  /**
   * Checks if the current user has a specific capability.
   * @param capabilityId The ID of the capability to check (e.g., 'USER', 'AGENT', 'READ', 'WRITE')
   * @return true if the capability is present in the SecurityContext, false otherwise
   */
  fun hasCapability(capabilityId: String): Boolean

  /**
   * Checks if the current user has any valid token/authentication.
   * @return true if authentication is present and authenticated, false otherwise
   */
  fun hasToken(): Boolean
}
