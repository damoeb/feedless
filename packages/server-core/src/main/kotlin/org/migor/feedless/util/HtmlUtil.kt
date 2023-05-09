package org.migor.feedless.util

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist

object HtmlUtil {
  fun html2text(html: String?): String = if (StringUtils.isBlank(html)) {
    ""
  } else {
    Jsoup.parse(cleanHtml(html!!)).text()
  }

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
}
