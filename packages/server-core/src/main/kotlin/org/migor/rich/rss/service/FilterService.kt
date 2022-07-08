package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.harvest.entryfilter.complex.generated.ComplexArticleFilter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class FilterService {

  private val log = LoggerFactory.getLogger(FilterService::class.simpleName)

  val samples = mapOf(
    "must include" to "justTheWord",
    "must not include" to "-badWord",
    "linkCount" to "linkCount > 0",
    "wordCount" to "wordCount > 10",
  )

  fun filter(
    corrId: String,
    article: Article,
    filterExpression: String
  ): Boolean {

    val filterExecutorOpt = Optional.ofNullable(createTakeIfRunner(corrId, filterExpression))
    return if (filterExecutorOpt.isPresent) {
      val matches = executeFilter(corrId, filterExpression, article)
      if (!matches) {
        log.info("[$corrId] Dropping article ${article.url}")
      }
      matches
    } else {
      true
    }
  }

  private fun createTakeIfRunner(corrId: String, filterExpression: String?): ComplexArticleFilter? {
    return try {
      filterExpression?.let { expr -> ComplexArticleFilter(expr.byteInputStream()) }
    } catch (e: Exception) {
      log.error("[$corrId] Invalid filter expression $filterExpression, ${e.message}")
      null
    }
  }

  private fun executeFilter(corrId: String, filterExecutor: String, article: Article): Boolean {
    return createTakeIfRunner(corrId, filterExecutor)!!.matches(article)
  }

  private fun matches(corrId: String, url: String, title: String, content: String, raw: String?, filter: String?): Boolean {
    return Optional.ofNullable(StringUtils.trimToNull(filter))
      .map {
        runCatching {
          ComplexArticleFilter(it.byteInputStream()).matches(url, title, content, raw)
        }.getOrElse { throw RuntimeException("Filter expression is invalid: ${it.message}") }
      }.orElse(true)
  }

  fun matches(corrId: String, article: RichArticle, filter: String?): Boolean {
    val matches = matches(corrId, article.url, article.title, article.contentText, article.contentRaw, filter)
    if (matches) {
      log.debug("keep ${article.url}")
    } else {
      log.debug("drop ${article.url}")
    }
    return matches
  }

//  fun matches(corrId: String, syndEntry: SyndEntry, filter: String?): Boolean {
//    return matches(corrId, syndEntry.title, syndEntry.description.value, filter)
//  }
}
