package org.migor.feedless.web

import net.dankito.readability4j.extended.Readability4JExtended
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.util.HtmlUtil.parseHtml
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class WebToArticleTransformer {

  private val log = LoggerFactory.getLogger(WebToArticleTransformer::class.simpleName)

  fun fromHtml(html: String, url: String): JsonItem {
    return fromDocument(parseHtml(html, url), url)
  }

  fun fromDocument(doc: Document, url: String): JsonItem {
    doc.select("script").remove()
    doc.select("style").remove()
    HtmlUtil.withAbsoluteUrls(doc)
    val item = JsonItem()
    item.url = url
    item.publishedAt = Date()
    item.contentRawMime = "text/html"
    item.url = url
    return extractContent(item, url, doc)
  }

  private fun extractContent(item: JsonItem, url: String, doc: Document): JsonItem {
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
