package org.migor.rich.rss.discovery

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.FeedDiscovery
import org.migor.rich.rss.api.dto.FeedDiscoveryOptions
import org.migor.rich.rss.api.dto.FeedDiscoveryResults
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.harvest.HarvestResponse
import org.migor.rich.rss.harvest.feedparser.FeedType
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.PuppeteerService
import org.migor.rich.rss.transform.CandidateFeedRule
import org.migor.rich.rss.transform.ExtendedFeedRule
import org.migor.rich.rss.transform.GenericFeedRule
import org.migor.rich.rss.transform.WebToFeedTransformer
import org.migor.rich.rss.util.FeedUtil
import org.migor.rich.rss.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import java.net.URL

@Service
class FeedDiscoveryService {
  private val log = LoggerFactory.getLogger(FeedDiscoveryService::class.simpleName)

  @Autowired
  lateinit var nativeFeedLocator: NativeFeedLocator

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var genericFeedLocator: GenericFeedLocator

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var feedService: FeedService

  fun asExtendedRule(
    corrId: String,
    homePageUrl: String,
    linkXPath: String,
    dateXPath: String?,
    contextXPath: String,
    extendContext: String,
    filter: String?,
    version: String,
    articleRecovery: ArticleRecoveryType,
    prerender: Boolean,
    puppeteerScript: String?
  ): ExtendedFeedRule {
    val rule = CandidateFeedRule(
      linkXPath = linkXPath,
      contextXPath = contextXPath,
      extendContext = extendContext,
      dateXPath = dateXPath
    )
    return ExtendedFeedRule(
      filter,
      version,
      homePageUrl,
      articleRecovery,
      feedUrl = webToFeedTransformer.createFeedUrl(URL(homePageUrl), rule, articleRecovery),
      rule,
      prerender,
      puppeteerScript
    )
  }


  fun discoverFeeds(
    corrId: String,
    homepageUrl: String,
    script: String? = null,
    prerender: Boolean = false,
    strictMode: Boolean = false,
  ): FeedDiscovery {
    fun toFeedDiscovery(
      url: String,
      mimeType: MimeType?,
      nativeFeeds: List<FeedReference>,
      relatedFeeds: List<Feed>,
      genericFeedRules: List<GenericFeedRule> = emptyList(),
      body: String = "",
      screenshot: String? = "",
      failed: Boolean = false,
      errorMessage: String? = null
    ): FeedDiscovery {
      return FeedDiscovery(
        options = FeedDiscoveryOptions(
          harvestUrl = url,
          originalUrl = homepageUrl,
          withJavaScript = prerender,
        ),
        results = FeedDiscoveryResults(
          mimeType = mimeType?.toString(),
          screenshot = screenshot,
          nativeFeeds = nativeFeeds,
          relatedFeeds = relatedFeeds,
          genericFeedRules = genericFeedRules,
          body = body,
          failed = failed,
          errorMessage = errorMessage
        )
      )
    }
    log.info("[$corrId] feeds/discover url=$homepageUrl, prerender=$prerender, strictMode=$strictMode")
    return runCatching {
      val url = httpService.parseUrl(homepageUrl)

      httpService.guardedHttpResource(corrId, url, 200, listOf("text/"))
      val staticResponse = httpService.httpGet(corrId, url, 200)

      val (feedType, mimeType) = FeedUtil.detectFeedTypeForResponse(staticResponse)

      val relatedFeeds = feedService.findRelatedByUrl(url)
      if (feedType !== FeedType.NONE) {
        val feed = feedService.parseFeed(corrId, HarvestResponse(url, staticResponse))
        log.info("[$corrId] is native-feed")
        toFeedDiscovery(
          url,
          mimeType,
          relatedFeeds = relatedFeeds,
          nativeFeeds = listOf(FeedReference(url = url, type = feedType, title = feed.title))
        )
      } else {
        if (prerender) {
          val puppeteerResponse = puppeteerService.prerender(corrId, url, StringUtils.trimToEmpty(script))
          val (nativeFeeds, genericFeedRules) = extractFeeds(corrId, puppeteerResponse.html!!, url, strictMode)
          toFeedDiscovery(
            url, mimeType,
            nativeFeeds = nativeFeeds,
            relatedFeeds = relatedFeeds,
            genericFeedRules = genericFeedRules,
            body = puppeteerResponse.html,
            screenshot = puppeteerResponse.screenshot,
            errorMessage = puppeteerResponse.errorMessage
          )
        } else {
          val body = String(staticResponse.responseBody)
          val (nativeFeeds, genericFeedRules) = extractFeeds(corrId, body, url, strictMode)
          toFeedDiscovery(
            url, mimeType,
            nativeFeeds = nativeFeeds,
            relatedFeeds = relatedFeeds,
            genericFeedRules = genericFeedRules,
            body = body
          )
        }
      }
    }.getOrElse {
      it.printStackTrace()
      log.error("[$corrId] Unable to discover feeds: ${it.message}")
      // todo mag return error code
      toFeedDiscovery(
        url = homepageUrl,
        nativeFeeds = emptyList(),
        relatedFeeds = emptyList(),
        mimeType = null,
        failed = true,
        errorMessage = it.message
      )
    }
  }


  private fun extractFeeds(
    corrId: String,
    html: String,
    url: String,
    strictMode: Boolean
  ): Pair<List<FeedReference>, List<GenericFeedRule>> {
    val document = HtmlUtil.parse(html)
    val genericFeedRules = genericFeedLocator.locateInDocument(corrId, document, url, strictMode)
    val nativeFeeds = nativeFeedLocator.locateInDocument(document, url)
    log.info("[$corrId] Found feedRules=${genericFeedRules.size} nativeFeeds=${nativeFeeds.size}")
    return Pair(nativeFeeds, genericFeedRules)
  }

  fun asExtendedRule(corrId: String, homepageUrl: String, rule: GenericFeedRule): ExtendedFeedRule {
    return asExtendedRule(corrId, homepageUrl,
      linkXPath = rule.linkXPath,
      dateXPath = rule.dateXPath,
      contextXPath = rule.contextXPath,
      extendContext = rule.extendContext,
      filter = "",
      version = "",
      articleRecovery = ArticleRecoveryType.FULL,
      prerender = false,
      puppeteerScript = null
      )
  }

}
