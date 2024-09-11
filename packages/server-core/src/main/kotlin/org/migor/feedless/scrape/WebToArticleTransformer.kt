package org.migor.feedless.scrape

import net.dankito.readability4j.extended.Readability4JExtended
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.util.HtmlUtil.parseHtml
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class WebToArticleTransformer {

  private val log = LoggerFactory.getLogger(WebToArticleTransformer::class.simpleName)

  suspend fun fromHtml(html: String, url: String): JsonItem {
    return fromDocument(parseHtml(html, url), url)
  }

  suspend fun fromDocument(doc: Document, url: String): JsonItem {
    doc.select("script").remove()
    doc.select("style").remove()
    HtmlUtil.withAbsoluteUrls(doc)
    val item = JsonItem()
    item.url = url
    item.publishedAt = LocalDateTime.now()
    item.contentRawMime = "text/html"
    item.url = url
    return extractContent(item, url, doc)
  }

  private suspend fun extractContent(item: JsonItem, url: String, doc: Document): JsonItem {
    val parser = Readability4JExtended(url, doc.html())
    val article = parser.parse()

    item.contentHtml = article.content
    item.contentText = article.textContent
    item.title = StringUtils.trimToEmpty(article.title)
//    extracted.contentMime = doc.title
//    extracted.dir = article.dir
    return item
  }

}
