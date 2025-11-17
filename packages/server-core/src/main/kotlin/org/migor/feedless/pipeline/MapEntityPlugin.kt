package org.migor.feedless.pipeline

import org.migor.feedless.data.jpa.document.DocumentEntity
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.document.Document
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector

interface MapEntityPlugin<T> : FeedlessPlugin {

  suspend fun mapEntity(
    document: Document,
    repository: Repository,
    params: T,
    logCollector: LogCollector
  ): Document

  suspend fun mapEntity(
    document: Document,
    repository: Repository,
    paramsJson: String?,
    logCollector: LogCollector
  ): Document

  suspend fun fromJson(jsonParams: String?): T
}
