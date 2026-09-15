package org.migor.feedless.config

object CacheNames {
  const val FEED_LONG_TTL = "feedResponseCache10min"
  const val FEED_SHORT_TTL = "feedResponseCache2Min"
  const val HTTP_RESPONSE = "httpResponseCache"
  const val AGENT_RESPONSE = "agentResponseCache"
  const val SERVER_SETTINGS = "graphqlResponseCache"
}
