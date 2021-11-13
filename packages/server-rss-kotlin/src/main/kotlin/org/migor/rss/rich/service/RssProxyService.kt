package org.migor.rss.rich.service

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.api.dto.FeedJsonDto
import org.migor.rss.rich.service.FeedService.Companion.absUrl
import org.migor.rss.rich.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import us.codecraft.xsoup.Xsoup
import java.util.*

@Service
class RssProxyService {

  private val log = LoggerFactory.getLogger(RssProxyService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  fun applyRule(homePageUrl: String, linkXPath: String, contextXPath: String, extendContext: String): FeedJsonDto {
    val response = httpService.httpGet(homePageUrl)
    val doc = Jsoup.parse(response.responseBody)

    val items = Xsoup.compile(contextXPath).evaluate(doc).elements
      .filterNotNull().mapNotNull { element: Element -> toArticle(element, linkXPath, homePageUrl) }

    return FeedJsonDto(
      id = homePageUrl,
      name = doc.title(),
      description = "",
      home_page_url = homePageUrl,
      date_published = Date(),
      items = items,
      feed_url = "http://localhost:8080/api/rss-proxy?linkXPath=$linkXPath...",
      expired = false
    )
  }

  private fun toArticle(element: Element, linkXPath: String, homePageUrl: String): ArticleJsonDto? {
    try {
      val linkElement = Xsoup.select(element, fixRelativePath(linkXPath)).elements.first()

      val url = absUrl(homePageUrl, linkElement.attr("href"))

      val title = Optional.ofNullable(StringUtils.trimToNull(linkElement.text())).orElse(FeedUtil.cleanMetatags(element.text().substring(0, 40)))

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
        date_published = Date()
      )
    } catch (e: Exception) {
      return null
    }
  }

  private fun fixRelativePath(xpath: String): String {
    return if (xpath.startsWith("./")) {
      xpath.replaceFirst("./", "//")
    } else {
      xpath
    }
  }
}
