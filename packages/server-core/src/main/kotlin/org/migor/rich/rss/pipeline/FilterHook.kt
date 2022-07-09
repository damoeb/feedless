package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database.enums.ArticleHookType
import org.migor.rich.rss.database.model.ArticleHookSpec
import org.migor.rich.rss.database.model.Bucket
import org.migor.rich.rss.database.model.NamespacedTag
import org.migor.rich.rss.harvest.ArticleSnapshot
import org.migor.rich.rss.service.FilterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("database")
@Service
class FilterHook : PipelineHook {

  @Autowired
  lateinit var filterService: FilterService

  override fun process(
    corrId: String,
    snapshot: ArticleSnapshot,
    bucket: Bucket,
    hookSpec: ArticleHookSpec,
    addTag: (NamespacedTag) -> Boolean,
    addData: (Pair<String, String>) -> String?
  ): Boolean {

    val article = snapshot.article
    return filterService.filter(corrId, article, hookSpec.context!!)
  }

  override fun type(): ArticleHookType = ArticleHookType.filter

}
