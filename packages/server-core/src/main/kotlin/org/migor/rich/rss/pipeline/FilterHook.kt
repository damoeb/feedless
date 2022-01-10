package org.migor.rss.rich.pipeline

import org.migor.rss.rich.database.enums.ArticleHookType
import org.migor.rss.rich.database.model.ArticleHookSpec
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.harvest.ArticleSnapshot
import org.migor.rss.rich.service.FilterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FilterHook : ArticleHook {

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
