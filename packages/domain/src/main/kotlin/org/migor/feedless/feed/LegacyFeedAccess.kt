package org.migor.feedless.feed

import org.migor.feedless.repository.RepositoryClaim

/**
 * A legacy token whose claimed repository [FeedService.requireLegacyTokenAccess] has checked; only that creates
 * one, so a URL-keyed cache cannot skip the check. Carries the resolved [claim] too, so callers reuse it instead
 * of resolving it again — a second, independent lookup could fail differently from the checked one.
 */
class LegacyFeedAccess internal constructor(val token: String?, val claim: RepositoryClaim?) {
  // the token is a credential, keep it out of logs
  override fun toString(): String = "LegacyFeedAccess"
}
