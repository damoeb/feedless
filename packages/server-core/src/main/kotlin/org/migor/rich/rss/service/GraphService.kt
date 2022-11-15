package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.rich.rss.database.models.ContentEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("database")
class GraphService {

  private val log = LoggerFactory.getLogger(GraphService::class.simpleName)

  fun links(content: ContentEntity) {
    val document = if (content.hasFulltext) {
      content.getContentOfMime("text/html")
    } else {
      content.description
    }.also { Optional.ofNullable(it).map { Jsoup.parse(it) }.orElse(null) }

    document?.let {
      val doc = Jsoup.parse(it)
      val urls = doc.body().select("a[href]")
        .map { link -> FeedService.absUrl(content.url!!, link.attr("href")) }
        .distinct()
        .filter { url -> StringUtils.isNotBlank(url) }
        .filter { url -> arrayOf("facebook.com", "twitter.com", "amazon.com", "patreon.com").none { blackListedUrl -> url.contains(blackListedUrl) } }

      urls.forEach { url -> log.info("${content.url} -> $url") }
    }
  }
}
