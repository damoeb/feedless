package org.migor.feedless.service

import io.micrometer.core.annotation.Timed
import org.migor.feedless.web.FetchOptions
import reactor.core.publisher.Mono

interface PuppeteerService {

  fun canPrerender(): Boolean

  @Timed
  fun prerender(
    corrId: String,
    options: FetchOptions,
  ): Mono<PuppeteerHttpResponse>
}

data class PuppeteerHttpResponse(
  val dataBase64: String?,
  val dataAscii: String?,
  val url: String,
  val errorMessage: String?,
  val isError: Boolean,
)
