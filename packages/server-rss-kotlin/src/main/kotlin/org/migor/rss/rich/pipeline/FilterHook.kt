package org.migor.rss.rich.pipeline

import org.migor.rss.rich.database.enums.ArticleHookType
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.ArticleHookSpec
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.harvest.ArticleSnapshot
import org.migor.rss.rich.harvest.entryfilter.generated.TakeEntryIfRunner
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class FilterHook : ArticleHook {

  private val log = LoggerFactory.getLogger(FilterHook::class.simpleName)

  override fun process(
    corrId: String,
    snapshot: ArticleSnapshot,
    bucket: Bucket,
    hookSpec: ArticleHookSpec,
    addTag: (NamespacedTag) -> Boolean,
    addData: (Pair<String, String>) -> String?
  ): Boolean {

    val article = snapshot.article
    val filterExecutorOpt = Optional.ofNullable(createTakeIfRunner(corrId, hookSpec.context))
    return if (filterExecutorOpt.isPresent) {
      val matches = executeFilter(corrId, hookSpec.context!!, article)
      if (!matches) {
        log.info("[$corrId] Dropping article ${article.url}")
      }
      matches
    } else {
      true
    }
  }

  override fun type(): ArticleHookType = ArticleHookType.filter

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
