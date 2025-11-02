package org.migor.feedless.connector.github;

import org.migor.feedless.capability.Capability

data class GithubCapability(val token: String) :
  Capability<String>("github.com", token)

