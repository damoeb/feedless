package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database.ArticleWithContext
import org.migor.rich.rss.database.enums.ArticleRefinementType
import org.migor.rich.rss.database.models.RefinementEntity

interface PreImportAction {
  fun process(
    corrId: String,
    snapshot: ArticleWithContext,
    refinement: RefinementEntity,
  ): Boolean

  fun type(): ArticleRefinementType
}
