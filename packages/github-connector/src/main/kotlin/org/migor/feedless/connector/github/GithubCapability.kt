package org.migor.feedless.connector.github;

import kotlinx.serialization.Serializable
import org.migor.feedless.capability.Capability
import org.migor.feedless.capability.CapabilityId

@Serializable
data class GithubCapability(val token: String) :
  Capability<String>(ID, token) {
  companion object {
    val ID: CapabilityId = CapabilityId("github.com")
  }
}

