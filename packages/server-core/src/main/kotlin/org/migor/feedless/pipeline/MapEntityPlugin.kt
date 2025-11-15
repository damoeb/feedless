package org.migor.feedless.pipeline

import org.migor.feedless.data.jpa.document.DocumentEntity
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.scrape.LogCollector

interface MapEntityPlugin<T> : FeedlessPlugin {

  suspend fun mapEntity(
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: T,
    logCollector: LogCollector
  ): DocumentEntity

  suspend fun mapEntity(
    document: DocumentEntity,
    repository: RepositoryEntity,
    paramsJson: String?,
    logCollector: LogCollector
  ): DocumentEntity

  suspend fun fromJson(jsonParams: String?): T
}
