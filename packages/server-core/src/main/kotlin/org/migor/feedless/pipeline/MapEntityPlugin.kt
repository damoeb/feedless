package org.migor.feedless.pipeline

import org.migor.feedless.data.jpa.models.DocumentEntity
import org.migor.feedless.data.jpa.models.RepositoryEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput

interface MapEntityPlugin : FeedlessPlugin {

  fun mapEntity(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  )

}
