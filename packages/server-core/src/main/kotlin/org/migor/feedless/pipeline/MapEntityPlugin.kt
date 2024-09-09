package org.migor.feedless.pipeline

import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.service.LogCollector

interface MapEntityPlugin : FeedlessPlugin {

  suspend fun mapEntity(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput,
    logCollector: LogCollector
  ): DocumentEntity

}
