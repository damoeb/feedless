package org.migor.rich.rss.transform

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.service.AnnouncementService
import org.migor.rich.rss.service.AuthToken
import org.migor.rich.rss.service.FilterService
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.PuppeteerService
import org.migor.rich.rss.util.FeedUtil
import org.migor.rich.rss.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@Service
class WebToFeedService {

  private val log = LoggerFactory.getLogger(WebToFeedService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @Autowired
  lateinit var articleRecovery: ArticleRecovery

  @Autowired
  lateinit var filterService: FilterService

  @Autowired
  lateinit var announcementService: AnnouncementService

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  @Value("\${app.publicUrl}")
  lateinit var appPublicUrl: String

  fun applyRule(
    corrId: String,
    extendedFeedRule: ExtendedFeedRule,
    token: AuthToken,
  ): RichFeed {
    val url = extendedFeedRule.homePageUrl
    val recovery = extendedFeedRule.recovery
    log.info("[${corrId}] applyRule url=${url}")

    val feedUrl = webToFeedTransformer.createFeedUrl(URL(url), extendedFeedRule.actualRule, recovery)
    validateVersion(extendedFeedRule.version)
    httpService.guardedHttpResource(corrId, url, 200, listOf("application/xml", "application/rss", "text/"))

    val markup = if (extendedFeedRule.prerender) {
      val puppeteerResponse = puppeteerService.prerender(corrId, url, StringUtils.trimToEmpty(extendedFeedRule.puppeteerScript), true)
      puppeteerResponse.html!!
    } else {
      val response = httpService.httpGet(corrId, url, 200)
      String(response.responseBody)
    }

    val doc = HtmlUtil.parse(markup)

    val items = webToFeedTransformer.getArticlesByRule(corrId, extendedFeedRule.actualRule, doc, URL(url))
      .asSequence()
      .filterIndexed { index, _ -> articleRecovery.shouldRecover(recovery, index) }
      .map { articleRecovery.recoverAndMerge(corrId, it, recovery) }
      .filter { filterService.matches(corrId, it, extendedFeedRule.filter) }
      .plus(announcementService.byToken(corrId, token, feedUrl))
      .toList()

    return createFeed(url, doc.title(), items, feedUrl)
  }

  private fun createFeed(
    homePageUrl: String,
    title: String,
    items: List<RichArticle>,
    feedUrl: String
  ) = RichFeed(
    id = feedUrl,
    title = title,
    "",
    home_page_url = homePageUrl,
    date_published = Date(),
    items = items,
    feed_url = feedUrl,
  )

  fun createMaintenanceFeed(corrId: String, homePageUrl: String, feedUrl: String, article: RichArticle): RichFeed {
    log.info("[${corrId}] falling back to maintenance feed")
    return createFeed(
      homePageUrl,
      "Maintenance",
      listOf(article),
      feedUrl
    )
  }

  private fun encode(param: String): String = URLEncoder.encode(
    param,
    StandardCharsets.UTF_8
  )

  fun createMaintenanceArticle(e: Throwable, url: String): RichArticle {
    // distinguish if an exception will be permanent or not, and only then send it
    return RichArticle(
      id = FeedUtil.toURI("maintenance-request", url, Date()),
      title = "Maintenance required",
      contentText = Optional.ofNullable(e.message).orElse(e.toString()),
      url = "${appPublicUrl}/?reason=${e.message}&url=${encode(url)}",
      publishedAt = Date(),
    )
  }

  fun createMaintenanceArticle(url: String): RichArticle {
    return RichArticle(
      id = FeedUtil.toURI("maintenance-request", url, Date()),
      title = "Maintenance required",
      contentText = "This feed is not supported anymore. Click the link to fix it.",
      url = "${appPublicUrl}/?reason=unsupported&url=${encode(url)}",
      publishedAt = Date(),
    )
  }

  private fun validateVersion(version: String) {
    if (version != propertyService.webToFeedVersion) {
      throw RuntimeException("Invalid webToFeed Version. Got ${version}, expected ${propertyService.webToFeedVersion}")
    }
  }
}
