package org.migor.feedless.feed

/** A legacy token whose claimed repository [FeedService.requireLegacyTokenAccess] has checked; only that creates one, so a URL-keyed cache cannot skip the check. */
class LegacyFeedAccess internal constructor(val token: String?) {
  // the token is a credential, keep it out of logs
  override fun toString(): String = "LegacyFeedAccess"
}
