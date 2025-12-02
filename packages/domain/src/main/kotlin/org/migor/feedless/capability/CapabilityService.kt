package org.migor.feedless.capability

// todo how is this compatible with non http-based requests
// should only life in the graphql/http layer, inside only groupId/userId are relevant
interface CapabilityService {

  /**
   * Checks if the current user has a specific capability.
   * @param capabilityId The ID of the capability to check (e.g., 'USER', 'AGENT', 'READ', 'WRITE')
   * @return true if the capability is present in the SecurityContext, false otherwise
   */
  fun hasCapability(capabilityId: CapabilityId): Boolean

  /**
   * Checks if the current user has any valid token/authentication.
   * @return true if authentication is present and authenticated, false otherwise
   */
  fun hasToken(): Boolean

  fun getCapability(capabilityId: CapabilityId): UnresolvedCapability?
}
