package org.migor.feedless.service

import io.micrometer.core.annotation.Timed
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ScrapeResponse
import reactor.core.publisher.Mono

interface PuppeteerService {

  fun canPrerender(): Boolean

  @Timed
  fun prerender(
    corrId: String,
    scrapeRequest: ScrapeRequest,
  ): Mono<ScrapeResponse>
}
