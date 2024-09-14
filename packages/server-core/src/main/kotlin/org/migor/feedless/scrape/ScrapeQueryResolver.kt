package org.migor.feedless.scrape

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.HttpFetchResponse
import org.migor.feedless.generated.types.ScrapeActionResponse
import org.migor.feedless.generated.types.ScrapeExtractResponse
import org.migor.feedless.generated.types.ScrapeOutputResponse
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.Record
import org.migor.feedless.session.useRequestContext
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.nio.charset.StandardCharsets

@DgsComponent
@Profile("${AppProfiles.scrape} & ${AppLayer.api}")
class ScrapeQueryResolver {

  private val log = LoggerFactory.getLogger(ScrapeQueryResolver::class.simpleName)

  @Autowired
  private lateinit var scrapeService: ScrapeService

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAnyAuthority('ANONYMOUS', 'READ', 'WRITE')")
  @Transactional(propagation = Propagation.NEVER)
  suspend fun scrape(
    dfe: DataFetchingEnvironment,
    @InputArgument data: SourceInput,
    @RequestHeader(ApiParams.corrId, required = false) cid: String,
  ): ScrapeResponse = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    val corrId = CryptUtil.handleCorrId(cid)
    log.debug("[$corrId] scrape $data")
    val scrapeRequest = data.fromDto()
    val logCollector = LogCollector()
    val scrapeOutput = scrapeService.scrape(corrId, scrapeRequest, logCollector)
    ScrapeResponse(
      ok = true,
      logs = logCollector.logs,
      errorMessage = null,
      outputs = scrapeOutput.outputs.map { it.toDto() }
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
      extract = fragment?.let {
        ScrapeExtractResponse(
          fragmentName = "extract",
          fragments = it.fragments,
          items = it.items?.map { it.toDto() },
          feeds = it.feeds
        )
      })
  )
}

private fun JsonItem.toDto() = Record(
  rawBase64 = rawBase64,
  rawMimeType = rawMimeType,
  html = html,
  text = text,
  title = title,
  url = url,
  tags = tags,
  createdAt = publishedAt.toMillis(),
  attachments = attachments.map { it.toDto() },
  id = id,
  imageUrl = imageUrl,
  publishedAt = publishedAt.toMillis(),
  startingAt = startingAt?.toMillis(),
  latLng = latLng?.toGeoPoint(),
)

private fun JsonPoint.toGeoPoint() = GeoPoint(
  lat = x,
  lon = y
)

private fun JsonAttachment.toDto() = org.migor.feedless.generated.types.Attachment(
  duration = duration,
  size = length,
  type = type,
  url = url,
)
