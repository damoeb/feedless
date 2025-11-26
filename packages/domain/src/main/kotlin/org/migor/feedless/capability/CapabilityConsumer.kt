package org.migor.feedless.capability

interface CapabilityConsumer {
  suspend fun expectsCapabilities(capabilityId: CapabilityId): Boolean
}
