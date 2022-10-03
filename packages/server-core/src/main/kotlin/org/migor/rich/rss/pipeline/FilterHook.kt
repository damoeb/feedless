package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database.enums.ArticleRefinementType
import org.migor.rich.rss.database.enums.NamespacedTag
import org.migor.rich.rss.database.models.BucketEntity
import org.migor.rich.rss.database.models.RefinementEntity
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
    bucket: BucketEntity,
    hookSpec: RefinementEntity,
    addTag: (NamespacedTag) -> Boolean,
    addData: (Pair<String, String>) -> String?
  ): Boolean {

    val article = snapshot.article
    return filterService.filter(corrId, article, hookSpec.context!!)
  }

  override fun type(): ArticleRefinementType = ArticleRefinementType.filter

}
