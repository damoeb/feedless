package org.migor.rss.rich.pipeline

import org.migor.rss.rich.database.enums.ArticleHookType
import org.migor.rss.rich.database.model.ArticleHookSpec
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.harvest.ArticleSnapshot

interface ArticleHook {
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
