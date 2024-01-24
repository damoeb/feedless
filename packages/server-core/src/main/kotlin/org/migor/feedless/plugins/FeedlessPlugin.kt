package org.migor.feedless.plugins

interface FeedlessPlugin {
  fun id(): String

  fun name(): String
  fun description(): String

  fun listed(): Boolean
}
