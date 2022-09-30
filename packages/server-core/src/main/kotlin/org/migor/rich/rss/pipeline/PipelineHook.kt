package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database2.enums.ArticleRefinementType
import org.migor.rich.rss.database.enums.NamespacedTag
import org.migor.rich.rss.database2.models.BucketEntity
import org.migor.rich.rss.database2.models.RefinementEntity
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
