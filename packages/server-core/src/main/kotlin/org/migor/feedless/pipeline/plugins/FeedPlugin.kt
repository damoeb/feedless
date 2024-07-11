package org.migor.feedless.pipeline.plugins

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.FeedParserService
import org.migor.feedless.feed.asRemoteNativeFeed
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.DOMExtract
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionData
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractResponse
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.util.JsonUtil
import org.migor.feedless.web.ExtendContext
import org.migor.feedless.web.GenericFeedRule
import org.migor.feedless.web.Selectors
import org.migor.feedless.web.WebExtractService
import org.migor.feedless.web.WebToFeedTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

@Service
@Profile(AppProfiles.scrape)
class FeedPlugin : FragmentTransformerPlugin {

  private val log = LoggerFactory.getLogger(FeedPlugin::class.simpleName)

  @Autowired
  private lateinit var webToFeedTransformer: WebToFeedTransformer

  @Lazy
  @Autowired
  private lateinit var feedParserService: FeedParserService

  @Autowired
  private lateinit var webExtractService: WebExtractService

  override fun id(): String = FeedlessPlugins.org_feedless_feed.name
  override fun listed() = true

  override fun transformFragment(
    corrId: String,
    action: ExecuteActionEntity,
    data: HttpResponse,
  ): PluginExecutionData {
    val executorParams = action.executorParams!!
    log.debug("[$corrId] transformFragment using selectors ${JsonUtil.gson.toJson(executorParams.org_feedless_feed)}")

    val document = HtmlUtil.parseHtml(data.responseBody.toString(StandardCharsets.UTF_8), data.url)

    val feed = (executorParams.org_feedless_feed?.generic?.let {
//    val extracts = webExtractService.extract(corrId, executorParams.org_feedless_feed!!.generic!!.toScrapeExtracts(), normalizeTags(document.body()))
//    val feed = extracts.asJsonFeed()

      webToFeedTransformer.getFeedBySelectors(
        corrId, it.toSelectors(),
        document, URL(data.url)
      )
    } ?: feedParserService.parseFeed(corrId, data)).asRemoteNativeFeed()
    log.debug("[$corrId] transformed to feed with ${feed.items!!.size} items")

    return PluginExecutionData(org_feedless_feed = feed)
  }

  override fun name(): String = "Feed"


}

//private fun ScrapeExtractResponse.asJsonFeed(): JsonFeed {
//  val jsonFeed = JsonFeed()
//  jsonFeed.id = ""
//  jsonFeed.title = "feed"
//  jsonFeed.websiteUrl = ""
//  jsonFeed.publishedAt = Date()
//  jsonFeed.items = fragments.map { it.asRichArticle() }
//  jsonFeed.feedUrl = ""
//
//  return jsonFeed
//}

//private fun ScrapeExtractFragment.asRichArticle(): JsonItem {
//  val article = JsonItem()
//
////  article.id = FeedUtil.toURI("article", articleUrl)
////  article.title = toTitle(linkText)
////  article.url = articleUrl
////  article.contentText = webToTextTransformer.extractText(content)
////  article.contentRawBase64 = withAbsUrls(content, url).selectFirst("body")!!.html()
////  article.contentRawMime = "text/html"
////  article.publishedAt = pubDate
////  article.startingAt = startingDate
//  return article
//}

private fun SelectorsInput.toScrapeExtracts(): DOMExtract {
  val extracts = mutableListOf(
    DOMExtract(
      fragmentName = JsonItem.URL,
      xpath = DOMElementByXPath(value = linkXPath),
      max = 1,
      emit = listOf(ScrapeEmit.text)
    )
  )
  if (StringUtils.isNotBlank(dateXPath)) {
    val fragmentName = if (dateIsStartOfEvent) {
      JsonItem.STARTING_AT
    } else {
      JsonItem.PUBLISHED_AT
    }
    extracts.add(
      DOMExtract(
        fragmentName = fragmentName,
        xpath = DOMElementByXPath(value = dateXPath),
        max = 1,
        emit = listOf(ScrapeEmit.date)
      )
    )
  }

  return DOMExtract(
    fragmentName = "feed",
    xpath = DOMElementByXPath(value = contextXPath),
    emit = listOf(ScrapeEmit.html, ScrapeEmit.text),
    extract = extracts
  )
}

private fun SelectorsInput.toSelectors(): Selectors {
  return GenericFeedRule(
    linkXPath = linkXPath,
    extendContext = ExtendContext.NONE,
    contextXPath = contextXPath,
    dateXPath = dateXPath,
    paginationXPath = paginationXPath,
    dateIsStartOfEvent = dateIsStartOfEvent,
    count = 0,
    score = 0.0,
  )
}
