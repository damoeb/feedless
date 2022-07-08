package org.migor.rich.rss.util

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object HtmlUtil {
  fun html2text(html: String?): String = if (StringUtils.isBlank(html)) {
    ""
  } else {
    Jsoup.parse(html!!).text()
  }

  fun parse(html: String): Document {
    return Jsoup.parse(html)
  }
}
