package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.data.jpa.source.actions.ExecuteActionEntity
import org.migor.feedless.feed.FeedParserService
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.MimeData
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractFragmentPart
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.scrape.GenericFeedRule
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.WebExtractService.Companion.MIME_URL
import org.migor.feedless.scrape.WebToFeedTransformer
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.nio.charset.StandardCharsets

data class FeedPluginParams(
  val generic: GenericFeedRule? = null,
)

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class FeedPlugin : FragmentTransformerPlugin {

  private val log = LoggerFactory.getLogger(FeedPlugin::class.simpleName)

  @Autowired
  private lateinit var webToFeedTransformer: WebToFeedTransformer

  @Lazy
  @Autowired
  private lateinit var feedParserService: FeedParserService

  override fun id(): String = FeedlessPlugins.org_feedless_feed.name
  override fun listed() = true

  override suspend fun transformFragment(
    action: ExecuteActionEntity,
    data: HttpResponse,
    logger: LogCollector,
  ): FragmentOutput {
    val executorParams = action.executorParams!!
    logger.log("transformFragment using selectors $executorParams")

    val document = HtmlUtil.parseHtml(data.responseBody.toString(StandardCharsets.UTF_8), data.url)

    val feed = if (FeedUtil.isFeed(data.contentType)) {
      feedParserService.parseFeed(data)
    } else {

      webToFeedTransformer.getFeedBySelectors(
        fromJson(executorParams.paramsJsonString)?.generic!!,
        document, URI(data.url),
        logger
      )
    }

    logger.log("transformed to feed with ${feed.items.size} items")

    return FragmentOutput(
      fragmentName = id(),
      items = feed.items,
      fragments = feed.links?.map {
        ScrapeExtractFragment(
          data = MimeData(data = it, mimeType = MIME_URL),
          uniqueBy = ScrapeExtractFragmentPart.data
        )
      }
    )
  }

  private fun fromJson(jsonParams: String?): FeedPluginParams? {
    return Gson().fromJson(jsonParams, FeedPluginParams::class.java)
  }

  override fun name(): String = "Feed"
}
