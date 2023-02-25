package org.migor.rich.rss.pipeline

import org.migor.rich.rss.data.jpa.ContentWithContext
import org.migor.rich.rss.data.jpa.enums.ArticleRefinementType
import org.migor.rich.rss.data.jpa.models.RefinementEntity

interface PreImportAction {
  fun process(
      corrId: String,
      snapshot: ContentWithContext,
      refinement: RefinementEntity,
  ): Boolean

  fun type(): ArticleRefinementType
}
