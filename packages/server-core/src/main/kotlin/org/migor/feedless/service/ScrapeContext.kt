package org.migor.feedless.service

import org.migor.feedless.common.HttpResponse
import org.migor.feedless.generated.types.FetchActionDebugResponse
import org.migor.feedless.generated.types.PluginExecutionResponse
import org.migor.feedless.generated.types.ScrapeExtractResponse

data class HttpFetchOutput(val response: HttpResponse, val debug: FetchActionDebugResponse)
data class ScrapeActionOutput(
  val index: Int,
  val fetch: HttpFetchOutput? = null,
  val extract: ScrapeExtractResponse? = null,
  val execute: PluginExecutionResponse? = null
)

data class ScrapeOutput(val outputs: List<ScrapeActionOutput>, val time: Int, val logs: List<String>)

class ScrapeContext {
  val headers = HashMap<String, String>()
  val outputs = mutableMapOf<Int, ScrapeActionOutput>()
  val logs = mutableListOf<String>()
}
