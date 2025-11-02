package org.migor.feedless.connector.github;

import org.migor.feedless.capability.Capability

data class GithubCapability(val token: String) :
  Capability<String>(ID, token) {
  companion object {
    val ID: String = "github.com"
  }
}

