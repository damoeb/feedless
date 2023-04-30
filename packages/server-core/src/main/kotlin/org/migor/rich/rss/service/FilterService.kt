package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.migor.rich.rss.harvest.entryfilter.simple.generated.SimpleArticleFilter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class FilterService {

  private val log = LoggerFactory.getLogger(FilterService::class.simpleName)

  fun matches(corrId: String, article: RichArticle, filter: String?): Boolean {
    return matches(article.url, article.title, article.contentText, article.contentRaw, filter)
  }

  fun matches(
    corrId: String,
    article: WebDocumentEntity,
    filter: String?
  ): Boolean {
    return matches(article.url, article.title!!, StringUtils.trimToEmpty(article.contentText), article.contentRaw, filter)
  }

  private fun matches(
    url: String,
    title: String,
    webDocument: String,
    raw: String?,
    filter: String?
  ): Boolean {
    val matches = Optional.ofNullable(StringUtils.trimToNull(filter))
      .map {
        runCatching {
          SimpleArticleFilter(it.byteInputStream()).Matches(title, webDocument)
        }.getOrElse { throw RuntimeException("Filter expression is invalid: ${it.message}") }
      }.orElse(true)

    if (matches) {
      log.debug("keep $url")
    } else {
      log.debug("drop $url")
    }
    return matches;
  }
}
