package org.migor.rich.rss.harvest.prerender

import io.micrometer.core.annotation.Timed
import org.migor.rich.rss.transform.GenericFeedFetchOptions

interface PuppeteerService {

  fun canPrerender(): Boolean
  fun hasHost(): Boolean

  @Timed
  fun prerender(
    corrId: String,
    options: GenericFeedFetchOptions,
  ): PuppeteerHttpResponse
}

data class PuppeteerHttpResponse(
  val html: String,
  val url: String,
)
