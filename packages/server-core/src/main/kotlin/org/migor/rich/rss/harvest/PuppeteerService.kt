package org.migor.rich.rss.harvest

import io.micrometer.core.annotation.Timed
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import reactor.core.publisher.Flux

interface PuppeteerService {

  fun canPrerender(): Boolean

  @Timed
  fun prerender(
    corrId: String,
    options: GenericFeedFetchOptions,
  ): Flux<PuppeteerHttpResponse>
}

data class PuppeteerHttpResponse(
  val html: String,
  val url: String,
  val errorMessage: String?,
  val isError: Boolean,
)
