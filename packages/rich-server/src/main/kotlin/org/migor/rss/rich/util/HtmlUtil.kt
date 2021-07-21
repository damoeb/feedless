package org.migor.rss.rich.util

import org.jsoup.Jsoup


object HtmlUtil {
  fun html2text(html: String?): String? {
    return Jsoup.parse(html).text()
  }
}
