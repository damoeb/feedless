package org.migor.rss.rich.util

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist


object HtmlUtil {
  fun cleanHtml(html: String?): String? = if (StringUtils.isBlank(html)) {
    null
  } else {
    Jsoup.clean(html, Whitelist.basicWithImages())
  }

  fun html2text(html: String?): String? = if (StringUtils.isBlank(html)) {
    null
  } else {
    Jsoup.parse(html).text()
  }
}
