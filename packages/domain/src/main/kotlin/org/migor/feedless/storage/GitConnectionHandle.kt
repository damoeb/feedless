package org.migor.feedless.storage

import org.migor.feedless.capability.CapabilityProvider

interface GitConnectionHandle : CapabilityProvider<GitConnectionCapability> {
  fun repositories(): List<GitRepository>
}
