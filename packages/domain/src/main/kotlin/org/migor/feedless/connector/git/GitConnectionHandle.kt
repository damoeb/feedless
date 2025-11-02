package org.migor.feedless.connector.git

import org.migor.feedless.capability.CapabilityProvider

interface GitConnectionHandle : CapabilityProvider<GitConnectionCapability> {
  fun repositories(): List<GitRepository>
}
