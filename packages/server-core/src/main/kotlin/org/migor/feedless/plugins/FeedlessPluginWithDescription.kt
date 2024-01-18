package org.migor.feedless.plugins

interface FeedlessPluginWithDescription: FeedlessPlugin {
  fun name(): String
  fun description(): String

}
