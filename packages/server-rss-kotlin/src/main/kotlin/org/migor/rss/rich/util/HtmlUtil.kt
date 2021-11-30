package org.migor.rss.rich.util

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

object HtmlUtil {
  fun cleanHtml(html: String?): String? = if (StringUtils.isBlank(html)) {
    null
  } else {
    Jsoup.parse(Jsoup.clean(html, Whitelist.relaxed())).body().html()
  }

  fun html2text(html: String?): String = if (StringUtils.isBlank(html)) {
    ""
  } else {
    Jsoup.parse(html).text()
  }
}
