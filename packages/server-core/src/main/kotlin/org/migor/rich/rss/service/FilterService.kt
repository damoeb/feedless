package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.harvest.entryfilter.complex.generated.TakeEntryIfRunner
import org.migor.rich.rss.harvest.entryfilter.simple.generated.SimpleArticleFilter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class FilterService {

  private val log = LoggerFactory.getLogger(FilterService::class.simpleName)

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

  private fun createTakeIfRunner(corrId: String, filterExpression: String?): TakeEntryIfRunner? {
    return try {
      filterExpression?.let { expr -> TakeEntryIfRunner(expr.byteInputStream()) }
    } catch (e: Exception) {
      log.error("[$corrId] Invalid filter expression $filterExpression, ${e.message}")
      null
    }
  }

  private fun executeFilter(corrId: String, filterExecutor: String, article: Article): Boolean {
    return createTakeIfRunner(corrId, filterExecutor)!!.takeIf(article)
  }

  private fun matches(corrId: String, title: String, content: String, filter: String?): Boolean {
    return Optional.ofNullable(StringUtils.trimToNull(filter))
      .map {
        runCatching {
          SimpleArticleFilter(it.byteInputStream()).Matches(title, content)
        }.getOrElse { throw RuntimeException("Filter expression is invalid: ${it.message}") }
      }.orElse(true)
  }

  fun matches(corrId: String, article: ArticleJsonDto, filter: String?): Boolean {
    return matches(corrId, article.title, article.content_text, filter)
  }

//  fun matches(corrId: String, syndEntry: SyndEntry, filter: String?): Boolean {
//    return matches(corrId, syndEntry.title, syndEntry.description.value, filter)
//  }
}
