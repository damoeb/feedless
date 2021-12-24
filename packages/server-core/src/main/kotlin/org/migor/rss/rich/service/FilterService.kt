package org.migor.rss.rich.service

import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.harvest.entryfilter.generated.TakeEntryIfRunner
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

}
