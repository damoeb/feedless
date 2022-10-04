package org.migor.rich.rss.database

import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.ImporterEntity
import org.migor.rich.rss.database.models.NativeFeedEntity

interface ArticleWithContext {
  var article: ArticleEntity
  var feed: NativeFeedEntity
  var importer: ImporterEntity
}
