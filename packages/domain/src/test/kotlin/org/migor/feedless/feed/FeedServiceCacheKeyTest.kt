package org.migor.feedless.feed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cache.annotation.Cacheable

/**
 * webToFeed and transformFeed are keyed by feedUrl alone, ignoring the LegacyFeedAccess.token the check ran
 * against. A caller passing null together with a feedUrl that carries another token would then hit that
 * token's cache entry. A real cache-hit test needs a Spring CacheManager, out of scope for this unit test;
 * this pins the SpEL key expression instead.
 */
class FeedServiceCacheKeyTest {

  @Test
  fun `webToFeed keys its cache entry on the checked access, not the url alone`() {
    val method = FeedService::class.java.methods.first { it.name == "webToFeed" }

    assertThat(method.getAnnotation(Cacheable::class.java).key).contains("access.token")
  }

  @Test
  fun `transformFeed keys its cache entry on the checked access, not the url alone`() {
    val method = FeedService::class.java.methods.first { it.name == "transformFeed" }

    assertThat(method.getAnnotation(Cacheable::class.java).key).contains("access.token")
  }
}
