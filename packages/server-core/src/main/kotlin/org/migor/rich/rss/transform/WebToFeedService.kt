package org.migor.rich.rss.transform

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.FeedJsonDto
import org.migor.rich.rss.database.repository.ArticleRepository
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.harvest.DeepArticleRecovery
import org.migor.rich.rss.service.FeedService.Companion.absUrl
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import us.codecraft.xsoup.Xsoup
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

  fun applyRule(
    corrId: String,
    homePageUrl: String,
    linkXPath: String,
    dateXPath: String?,
    contextXPath: String,
    extendContext: String,
    excludeUrlsContaining: List<String>,
    version: String,
    articleRecovery: ArticleRecovery
  ): FeedJsonDto {
    log.info("[${corrId}] applyRule for $homePageUrl, articleRecovery=$articleRecovery")
    validateVersion(version)
    val response = httpService.httpGet(corrId, homePageUrl, 200)
    val doc = Jsoup.parse(response.responseBody)

    val rule = CandidateFeedRule(
      linkXPath = linkXPath,
      contextXPath = contextXPath,
      extendContext = extendContext,
      dateXPath = dateXPath
    )

    val items = webToFeedTransformer.getArticlesByRule(corrId, rule, doc, URL(homePageUrl))
      .filterIndexed { index, _ -> deepArticleRecovery.shouldRecover(articleRecovery, index) }
      .map { deepArticleRecovery.recoverArticle(corrId, it, articleRecovery) }

    return FeedJsonDto(
        id = homePageUrl,
        name = doc.title(),
        description = "",
        home_page_url = homePageUrl,
        date_published = Date(),
        items = items,
        feed_url = webToFeedTransformer.convertRuleToFeedUrl(URL(homePageUrl), rule, articleRecovery),
        expired = false,
    )
  }

  private fun validateVersion(version: String) {
    if (version != propertyService.webToFeedVersion) {
      throw RuntimeException("Invalid webToFeed Version. Got ${version}, expected ${propertyService.webToFeedVersion}")
    }
  }

  private fun toArticle(element: Element, linkXPath: String, homePageUrl: String): ArticleJsonDto? {
    try {
      val linkElement = Xsoup.select(element, fixRelativePath(linkXPath)).elements.first()!!

      val url = absUrl(homePageUrl, linkElement.attr("href"))

      val title = Optional.ofNullable(StringUtils.trimToNull(linkElement.text()))
        .orElse(FeedUtil.cleanMetatags(element.text().substring(0, 40)))

      return ArticleJsonDto(
        id = url,
        title = title,
        url = url,
        author = null,
        tags = null,
        enclosures = null,
        commentsFeedUrl = null,
        main_image_url = null,
        content_text = element.text(),
        content_raw = element.html(),
        content_raw_mime = "text/html",
        date_published = tryRecoverPubDate(url)
      )
    } catch (e: Exception) {
      return null
    }
  }

  private fun tryRecoverPubDate(url: String): Date {
    return Optional.ofNullable(articleRepository).map { repo ->  Optional.ofNullable(repo.findByUrl(url)).map { article -> article.pubDate }.orElse(Date())}.orElse(Date())
  }

  private fun fixRelativePath(xpath: String): String {
    return if (xpath.startsWith("./")) {
      xpath.replaceFirst("./", "//")
    } else {
      xpath
    }
  }
}
