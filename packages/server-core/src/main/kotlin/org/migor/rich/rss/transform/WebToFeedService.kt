package org.migor.rich.rss.transform

import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.FeedJsonDto
import org.migor.rich.rss.database.repository.ArticleRepository
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.harvest.DeepArticleRecovery
import org.migor.rich.rss.service.FilterService
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.util.FeedUtil
import org.migor.rich.rss.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*

@Service
class WebToFeedService {

  private val log = LoggerFactory.getLogger(WebToFeedService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired(required = false)
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @Autowired
  lateinit var deepArticleRecovery: DeepArticleRecovery

  @Autowired
  lateinit var filterService: FilterService

  fun applyRule(
    corrId: String,
    extendedFeedRule: ExtendedFeedRule,
  ): FeedJsonDto {
    val url = extendedFeedRule.homePageUrl
    val recovery = extendedFeedRule.recovery
    log.info("[${corrId}] applyRule url=${url}")

    val feedUrl = webToFeedTransformer.createFeedUrl(URL(url), extendedFeedRule.actualRule, recovery)
    validateVersion(extendedFeedRule.version)
    httpService.httpHeadAssertions(corrId, url, 200, listOf("application/xml", "application/rss", "text/"))
    val response = httpService.httpGet(corrId, url, 200)
    val doc = HtmlUtil.parse(response.responseBody)

    val items = webToFeedTransformer.getArticlesByRule(corrId, extendedFeedRule.actualRule, doc, URL(url))
      .asSequence()
      .filterIndexed { index, _ -> deepArticleRecovery.shouldRecover(recovery, index) }
      .map { deepArticleRecovery.recoverArticle(corrId, it, recovery) }
      .filter { filterService.matches(it, extendedFeedRule.filter) }

    return createFeed(url, doc.title(), url, items.toList(), feedUrl)
  }

  fun asExtendedRule(
    corrId: String,
    homePageUrl: String,
    linkXPath: String,
    dateXPath: String?,
    contextXPath: String,
    extendContext: String,
    filter: String?,
    version: String,
    articleRecovery: ArticleRecovery
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
      rule
    )
  }

  private fun createFeed(
    id: String,
    name: String,
    homePageUrl: String,
    items: List<ArticleJsonDto>,
    feedUrl: String
  ) = FeedJsonDto(
    id,
    name,
    "",
    homePageUrl,
    date_published = Date(),
    items = items,
    feed_url = feedUrl,
    expired = false,
  )

  fun createMaintenanceFeed(corrId: String, e: Throwable, homePageUrl: String, feedUrl: String): FeedJsonDto {
    log.info("[${corrId}] falling back to maintenance feed due to ${e.message}")
    return createFeed(
      FeedUtil.toURI("maintenance-feed", Date(), feedUrl),
      URL(homePageUrl).host,
      homePageUrl,
      listOf(createExceptionArticle(e, homePageUrl)),
      feedUrl
    )
  }

  private fun createExceptionArticle(e: Throwable, url: String): ArticleJsonDto {
    // distinguish if an exception will be permanent or not, and only then send it
    return ArticleJsonDto(
      id = FeedUtil.toURI("maintenance-request", Date(), url),
      title = "Maintenance required",
      content_text = e.message!!,
      url = "http://maintenance-url.com/#whattodoaboutit${Date().toString()}",
      date_published = Date(),
    )
  }

  private fun validateVersion(version: String) {
    if (version != propertyService.webToFeedVersion) {
      throw RuntimeException("Invalid webToFeed Version. Got ${version}, expected ${propertyService.webToFeedVersion}")
    }
  }
}
