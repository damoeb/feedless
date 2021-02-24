package org.migor.rss.rich

import org.jsoup.Jsoup


object HtmlUtil {
  fun html2text(html: String?): String? {
    return Jsoup.parse(html).text()
  }
}
