package org.migor.rss.rich.service

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.api.dto.FeedJsonDto
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.parser.CandidateFeedRule
import org.migor.rss.rich.parser.WebToFeedParser
import org.migor.rss.rich.service.FeedService.Companion.absUrl
import org.migor.rss.rich.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import us.codecraft.xsoup.Xsoup
import java.net.URL
import java.util.*

@Service
class RssProxyService {

  private val log = LoggerFactory.getLogger(RssProxyService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var webToFeedParser: WebToFeedParser

  fun applyRule(
    homePageUrl: String,
    linkXPath: String,
    contextXPath: String,
    extendContext: String,
    excludeUrlsContaining: List<String>,
    correlationId: String
  ): FeedJsonDto {
    log.info("[${correlationId}] applyRule ${homePageUrl}")
    val response = httpService.httpGet(correlationId, homePageUrl, 200)
    val doc = Jsoup.parse(response.responseBody)
    val rule = CandidateFeedRule(
      linkXPath = linkXPath,
      contextXPath = contextXPath,
      extendContext = extendContext
    )

    val items = Xsoup.compile(contextXPath).evaluate(doc).elements
      .mapNotNull { element: Element -> toArticle(element, linkXPath, homePageUrl) }
      .filter { articleJson -> !excludeUrlsContaining.stream().anyMatch { excludedUrl -> articleJson.url.contains(excludedUrl) } }

    return FeedJsonDto(
      id = homePageUrl,
      name = doc.title(),
      description = "",
      home_page_url = homePageUrl,
      date_published = Date(),
      items = items,
      feed_url = webToFeedParser.convertRuleToFeedUrl(URL(homePageUrl), rule),
      expired = false
    )
  }

  private fun toArticle(element: Element, linkXPath: String, homePageUrl: String): ArticleJsonDto? {
    try {
      val linkElement = Xsoup.select(element, fixRelativePath(linkXPath)).elements.first()

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
    return Optional.ofNullable(articleRepository.findByUrl(url)).map { article -> article.pubDate }.orElse(Date())
  }

  private fun fixRelativePath(xpath: String): String {
    return if (xpath.startsWith("./")) {
      xpath.replaceFirst("./", "//")
    } else {
      xpath
    }
  }
}
