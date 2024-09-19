package org.migor.feedless.pipeline

import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.scrape.LogCollector

interface MapEntityPlugin : FeedlessPlugin {

  suspend fun mapEntity(
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput,
    logCollector: LogCollector
  ): DocumentEntity

}
