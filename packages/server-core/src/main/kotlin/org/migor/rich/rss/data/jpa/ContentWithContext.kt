package org.migor.rich.rss.data.jpa

import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.ImporterEntity
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity

interface ContentWithContext {
  var content: ContentEntity
  var feed: NativeFeedEntity
  var importer: ImporterEntity
}
