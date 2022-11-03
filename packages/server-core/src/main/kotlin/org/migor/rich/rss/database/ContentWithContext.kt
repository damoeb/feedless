package org.migor.rich.rss.database

import org.migor.rich.rss.database.models.ArticleContentEntity
import org.migor.rich.rss.database.models.ImporterEntity
import org.migor.rich.rss.database.models.NativeFeedEntity

interface ContentWithContext {
  var content: ArticleContentEntity
  var feed: NativeFeedEntity
  var importer: ImporterEntity
}
