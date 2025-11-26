package org.migor.feedless.connector.github;

import org.migor.feedless.capability.Capability
import org.migor.feedless.capability.CapabilityId

data class GithubCapability(val token: String) :
  Capability<String>(ID, token) {
  companion object {
    val ID: CapabilityId = CapabilityId("github.com")
  }
}

