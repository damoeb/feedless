package org.migor.rich.rss.database

import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.models.ImporterEntity
import org.migor.rich.rss.database.models.NativeFeedEntity

interface ContentWithContext {
  var content: ContentEntity
  var feed: NativeFeedEntity
  var importer: ImporterEntity
}
