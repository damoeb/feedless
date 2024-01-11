package org.migor.feedless.service

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.harvest.entryfilter.simple.SimpleArticle
import org.migor.feedless.harvest.entryfilter.simple.generated.SimpleArticleFilter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL

@Service
class FilterService {

  private val log = LoggerFactory.getLogger(FilterService::class.simpleName)

  fun matches(corrId: String, article: RichArticle, filter: String?): Boolean {
    return filter?.let {matches(article.url, article.title, article.contentText, linkCount(article), filter)} ?: true
  }

  fun matches(
    corrId: String,
    article: WebDocumentEntity,
    filter: String?
  ): Boolean {
    return filter?.let { matches(article.url, article.contentTitle!!, StringUtils.trimToEmpty(article.contentText), linkCount(article), filter)} ?: true
  }
  private fun linkCount(article: RichArticle): Int {
    return if (article.contentRawMime?.contains("html") == true) {
      linkCountFromHtml(article.url, article.contentRaw!!)
    } else {
      linkCountFromText(article.url, article.contentText)
    }
  }

  private fun linkCount(article: WebDocumentEntity): Int {
    return article.contentHtml()?.let {
      linkCountFromHtml(article.url, it)
    } ?: linkCountFromText(article.url, StringUtils.trimToEmpty(article.contentText))
  }

  private fun linkCountFromText(url: String, contentText: String): Int {
    return contentText.split("https://").size
  }

  private fun linkCountFromHtml(url: String, contentHtml: String): Int {
    val doc = Jsoup.parse(contentHtml)
    return doc.select("a[href]")
      .map { absUrl(url, it.attr("href")) }
      .distinct()
      .size
  }

  private fun absUrl(baseUrl: String, relativeUrl: String): String {
    return try {
      URL(URL(baseUrl), relativeUrl).toURI().toString()
    } catch (e: Exception) {
      relativeUrl
    }
  }


  private fun matches(
    url: String,
    title: String,
    body: String,
    linkCount: Int,
    filter: String
  ): Boolean {
    val matches = runCatching {
      val article = SimpleArticle(title, url, body, linkCount)
      SimpleArticleFilter(filter.byteInputStream()).matches(article)
    }.getOrElse { throw RuntimeException("Filter expression is invalid: ${it.message}") }

    if (matches) {
      log.debug("keep $url")
    } else {
      log.debug("drop $url")
    }
    return matches
  }
}
