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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class WebToArticleTransformer(
  private val pageInspectionService: PageInspectionService
) {

  private val log = LoggerFactory.getLogger(WebToArticleTransformer::class.simpleName)

  suspend fun fromHtml(html: String, url: String, summaryOnly: Boolean): JsonItem {
    val doc = parseHtml(html, url)
    doc.select("script").remove()
    doc.select("style").remove()
    HtmlUtil.withAbsoluteUrls(doc)
    val item = JsonItem()
    item.url = url
    item.publishedAt = LocalDateTime.now()
    item.rawMimeType = "text/html"
    item.url = url
    return extractContent(item, url, doc, summaryOnly)
  }

  private suspend fun extractContent(item: JsonItem, url: String, doc: Document, summaryOnly: Boolean): JsonItem {
    val parser = Readability4JExtended(url, doc.html())
    val article = parser.parse()

    item.html = article.content
    item.text = article.textContent
    item.title = StringUtils.trimToEmpty(article.title)
//    item.contentMime = doc.title
//    item.dir = article.dir

    return if (summaryOnly) {
      val result = pageInspectionService.fromDocument(doc)
      item.title = arrayOf(result.valueOf(PageInspection.TITLE), article.title, "no-title-available").firstNotNullOfOrNull { StringUtils.trimToNull(it) }!!
      item.text = arrayOf(result.valueOf(PageInspection.DESCRIPTION), StringUtils.abbreviate(article.textContent, "...", 150), "").firstNotNullOfOrNull{ StringUtils.trimToNull(it) }
      item.language = result.valueOf(PageInspection.LANG)
      item.imageUrl = result.valueOf(PageInspection.IMAGE_URL)
      item
    } else {
      item
    }
  }

}
