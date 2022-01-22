package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database.enums.ArticleHookType
import org.migor.rich.rss.database.model.ArticleHookSpec
import org.migor.rich.rss.database.model.Bucket
import org.migor.rich.rss.database.model.NamespacedTag
import org.migor.rich.rss.harvest.ArticleSnapshot

interface PipelineHook {
  fun process(
    corrId: String,
    snapshot: ArticleSnapshot,
    bucket: Bucket,
    hookSpec: ArticleHookSpec,
    addTag: (NamespacedTag) -> Boolean,
    addData: (Pair<String, String>) -> String?
  ): Boolean

  fun type(): ArticleHookType
}
