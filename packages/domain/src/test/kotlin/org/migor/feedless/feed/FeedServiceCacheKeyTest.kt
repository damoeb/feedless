package org.migor.feedless.feed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cache.annotation.Cacheable

/** Pins the SpEL cache key of webToFeed/transformFeed to the access token too, not just feedUrl — a real cache-hit test needs a Spring CacheManager. */
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
