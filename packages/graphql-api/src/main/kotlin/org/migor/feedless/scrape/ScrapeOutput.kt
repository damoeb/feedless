package org.migor.feedless.scrape

import org.migor.feedless.common.HttpResponse
import org.migor.feedless.generated.types.FetchActionDebugResponse
import org.migor.feedless.pipeline.FragmentOutput

data class HttpFetchOutput(val response: HttpResponse, val debug: FetchActionDebugResponse)
data class ScrapeActionOutput(
  val index: Int,
  val fetch: HttpFetchOutput? = null,
  val fragment: FragmentOutput? = null,
)

data class ScrapeOutput(
  val outputs: List<ScrapeActionOutput>,
  val time: Int
)
