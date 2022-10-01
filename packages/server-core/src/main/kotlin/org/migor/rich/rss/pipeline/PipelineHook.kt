package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database.enums.ArticleRefinementType
import org.migor.rich.rss.database.enums.NamespacedTag
import org.migor.rich.rss.database.models.BucketEntity
import org.migor.rich.rss.database.models.RefinementEntity
import org.migor.rich.rss.harvest.ArticleSnapshot

interface PipelineHook {
  fun process(
    corrId: String,
    snapshot: ArticleSnapshot,
    bucket: BucketEntity,
    hookSpec: RefinementEntity,
    addTag: (NamespacedTag) -> Boolean,
    addData: (Pair<String, String>) -> String?
  ): Boolean

  fun type(): ArticleRefinementType
}
