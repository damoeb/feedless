package org.migor.feedless.web

import net.dankito.readability4j.extended.Readability4JExtended
import org.jsoup.nodes.Document
import org.migor.feedless.util.HtmlUtil.parseHtml
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WebToArticleTransformer {

  private val log = LoggerFactory.getLogger(WebToArticleTransformer::class.simpleName)

  fun fromHtml(html: String, url: String): ExtractedArticle {
    return fromDocument(parseHtml(html, url), url)
  }

  fun fromDocument(doc: Document, url: String): ExtractedArticle {
    doc.select("script").remove()
    doc.select("style").remove()
    doc.select("[href]").forEach { a -> a.attr("href", a.absUrl("href")) }
    val extracted = ExtractedArticle(url)
    extracted.contentMime = "text/html"
    extracted.url = url
    return extractContent(extracted, url, doc)
  }

  private fun extractContent(extracted: ExtractedArticle, url: String, doc: Document): ExtractedArticle {
    val parser = Readability4JExtended(url, doc.html())
    val article = parser.parse()

    extracted.content = article.content
    extracted.contentText = article.textContent
    extracted.title = article.title
//    extracted.contentMime = doc.title
//    extracted.dir = article.dir
    return extracted
  }

}
