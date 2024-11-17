package org.migor.feedless.pipeline

import org.migor.feedless.actions.PluginExecutionJsonEntity
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.scrape.LogCollector

interface MapEntityPlugin : FeedlessPlugin {

  suspend fun mapEntity(
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionJsonEntity,
    logCollector: LogCollector
  ): DocumentEntity

}
