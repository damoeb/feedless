package org.migor.feedless.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist

object HtmlUtil {

  fun parseHtml(html: String, baseUrl: String): Document {
    val document = Jsoup.parse(html, baseUrl)
    document.select(".hidden").remove()
    return document
  }

  fun cleanHtml(html: String): String {
    return Jsoup.clean(
      html, Safelist.relaxed()
        .addTags("div", "section", "header", "footer", "figure", "picture", "figcaption")
        .addAttributes("img", "src")
        .addAttributes("a", "href")
        .addAttributes("div", "role")
    )
  }

  fun withAbsoluteUrls(doc: Document) {
    doc.select("[href]").forEach { a -> a.attr("href", a.absUrl("href")) }
  }
}
