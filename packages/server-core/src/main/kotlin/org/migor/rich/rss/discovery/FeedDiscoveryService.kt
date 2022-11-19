package org.migor.rich.rss.discovery

import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.migor.rich.rss.api.dto.FeedDiscovery
import org.migor.rich.rss.api.dto.FeedDiscoveryOptions
import org.migor.rich.rss.api.dto.FeedDiscoveryResults
import org.migor.rich.rss.database.models.NativeFeedEntity
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
      relatedFeeds: List<NativeFeedEntity>,
      genericFeedRules: List<GenericFeedRule> = emptyList(),
      body: String = "",
      title: String = "",
      description: String = "",
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
          title = title,
          description = description,
          failed = failed,
          errorMessage = errorMessage
        )
      )
    }
    log.info("[$corrId] feeds/discover url=$homepageUrl, prerender=$prerender, strictMode=$strictMode")
    return runCatching {
      val url = rewriteUrl(corrId, httpService.prefixUrl(homepageUrl.trim()))

      httpService.guardedHttpResource(corrId, url, 200, listOf("text/", "application/xml", "application/rss", "application/atom", "application/rdf"))
      val staticResponse = httpService.httpGetCaching(corrId, url, 200)

      val (feedType, mimeType) = FeedUtil.detectFeedTypeForResponse(staticResponse)

      val relatedFeeds = feedService.findRelatedByUrl(url)
      if (feedType !== FeedType.NONE) {
        val feed = feedService.parseFeed(corrId, HarvestResponse(url, staticResponse))
        log.info("[$corrId] is native-feed")
        toFeedDiscovery(
          url,
          mimeType,
          relatedFeeds = relatedFeeds,
          nativeFeeds = listOf(FeedReference(url = url, type = feedType, title = feed.title, description = feed.description))
        )
      } else {
        if (prerender) {
          val puppeteerResponse = puppeteerService.prerender(corrId, url, StringUtils.trimToEmpty(script))
          val document = HtmlUtil.parse(puppeteerResponse.html!!)
          val (nativeFeeds, genericFeedRules) = extractFeeds(corrId, document, url, strictMode)
          toFeedDiscovery(
            url,
            mimeType,
            nativeFeeds = nativeFeeds,
            relatedFeeds = relatedFeeds,
            genericFeedRules = genericFeedRules,
            body = puppeteerResponse.html,
            screenshot = puppeteerResponse.screenshot,
            errorMessage = puppeteerResponse.errorMessage,
            title = document.title(),
            description = document.select("meta[name=description]").text()
          )
        } else {
          val body = String(staticResponse.responseBody)
          val document = HtmlUtil.parse(body)
          val (nativeFeeds, genericFeedRules) = extractFeeds(corrId, document, url, strictMode)
          toFeedDiscovery(
            url, mimeType,
            nativeFeeds = nativeFeeds,
            relatedFeeds = relatedFeeds,
            genericFeedRules = genericFeedRules,
            body = body,
            title = document.title(),
            description = document.select("meta[name=description]").text()
          )
        }
      }
    }.getOrElse {
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

  private fun rewriteUrl(corrId: String, url: String): String {
    val rewrite = url.replace("https://twitter.com", propertyService.nitterHost)
    if (rewrite != url) {
      log.info("[$corrId] rewrote url $url -> $rewrite")
    }
    return rewrite
  }


  private fun extractFeeds(
    corrId: String,
    document: Document,
    url: String,
    strictMode: Boolean
  ): Pair<List<FeedReference>, List<GenericFeedRule>> {
    val genericFeedRules = genericFeedLocator.locateInDocument(corrId, document, url, strictMode)
    val nativeFeeds = nativeFeedLocator.locateInDocument(document, url)
    log.info("[$corrId] Found feedRules=${genericFeedRules.size} nativeFeeds=${nativeFeeds.size}")
    return Pair(nativeFeeds, genericFeedRules)
  }

  fun asExtendedRule(corrId: String, homepageUrl: String, rule: GenericFeedRule): ExtendedFeedRule {
    return asExtendedRule(
      corrId, homepageUrl,
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
