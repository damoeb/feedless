package org.migor.feedless.capability

data class UnresolvedCapability(override val capabilityId: CapabilityId, override val capabilityPayload: String) :
  Capability<String>(capabilityId, capabilityPayload)
