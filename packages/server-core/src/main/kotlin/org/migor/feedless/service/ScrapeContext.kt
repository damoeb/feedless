package org.migor.feedless.service

import org.migor.feedless.common.HttpResponse
import org.migor.feedless.generated.types.FetchActionDebugResponse
import org.migor.feedless.generated.types.LogStatement
import org.migor.feedless.pipeline.FragmentOutput
import org.slf4j.Logger
import java.util.*

data class HttpFetchOutput(val response: HttpResponse, val debug: FetchActionDebugResponse)
data class ScrapeActionOutput(
  val index: Int,
  val fetch: HttpFetchOutput? = null,
  val fragment: FragmentOutput? = null,
)

data class ScrapeOutput(val outputs: List<ScrapeActionOutput>, val time: Int, val logs: List<LogStatement>)

class ScrapeContext(private val logger: Logger) {
  fun outputsAsList(): List<ScrapeActionOutput> {
    return outputs.toList().sortedBy { it.first }.map { it.second }
  }

  fun setOutputAt(index: Int, output: ScrapeActionOutput) {
    outputs[index] = output
  }

  fun lastOutput(): ScrapeActionOutput {
    return outputs[outputs.keys.sortedDescending().first()]!!
  }

  fun firstUrl(): String? {
    return outputs.values.filter { it.fetch != null }.map { it.fetch!!.response.url }.firstOrNull()
  }

  fun hasOutputAt(index: Int): Boolean {
    return this.outputs[index] != null
  }

  fun info(message: String) {
    logger.debug(message)
    logs.add(LogStatement(
      time = Date().time,
      message = message,
    ))
  }

  val headers = HashMap<String, String>()
  private val outputs = mutableMapOf<Int, ScrapeActionOutput>()
  val logs = mutableListOf<LogStatement>()
}
