package org.migor.feedless.pipeline

interface FeedlessPlugin {
  fun id(): String

  fun name(): String

  fun listed(): Boolean
}
