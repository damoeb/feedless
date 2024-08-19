package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.types.HttpFetchResponse
import org.migor.feedless.generated.types.PluginExecutionResponse
import org.migor.feedless.generated.types.ScrapeActionResponse
import org.migor.feedless.generated.types.ScrapeOutputResponse
import org.migor.feedless.generated.types.ScrapeRequestInput
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.service.ScrapeActionOutput
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.nio.charset.StandardCharsets

@DgsComponent
@Profile("${AppProfiles.scrape} & ${AppProfiles.api}")
class ScrapeQueryResolver {

  private val log = LoggerFactory.getLogger(ScrapeQueryResolver::class.simpleName)

  @Autowired
  private lateinit var scrapeService: ScrapeService

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAnyAuthority('ANONYMOUS', 'READ', 'WRITE')")
  @Transactional(propagation = Propagation.NEVER)
  suspend fun scrape(
    @InputArgument data: ScrapeRequestInput,
    @RequestHeader(ApiParams.corrId, required = false) cid: String,
  ): ScrapeResponse = coroutineScope {
    val corrId = handleCorrId(cid)
    log.info("[$corrId] scrape $data")
    val scrapeRequest = data.fromDto()
    val output = scrapeService.scrape(corrId, scrapeRequest)
    ScrapeResponse(
      failed = false,
      logs = output.logs,
      errorMessage = null,
      outputs = output.outputs.map { it.toDto() }
    )
  }
}

private fun ScrapeActionOutput.toDto(): ScrapeOutputResponse {
  return ScrapeOutputResponse(
    index = index,
    response = ScrapeActionResponse(
      fetch = fetch?.let {
        HttpFetchResponse(
          data = it.response.responseBody.toString(StandardCharsets.UTF_8),
          debug = it.debug
        )
      },
      extract = extract,
      execute = execute?.let {
        PluginExecutionResponse(
          pluginId = it.pluginId,
          data = it.data
        )
      })
  )
}
