package org.migor.feedless.feed

import org.migor.feedless.repository.RepositoryClaim

/** A legacy token whose [claim] [FeedService.requireLegacyTokenAccess] has already checked and resolved, so callers reuse it instead of a second, independently-fallible lookup. */
class LegacyFeedAccess internal constructor(val token: String?, val claim: RepositoryClaim?) {
  // the token is a credential, keep it out of logs
  override fun toString(): String = "LegacyFeedAccess"
}
