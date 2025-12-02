package org.migor.feedless.source

import org.migor.feedless.actions.ScrapeAction

interface ScrapeActionRepository {
  suspend fun findAllBySourceId(id: SourceId): List<ScrapeAction>
  suspend fun saveAll(actions: List<ScrapeAction>)
  suspend fun deleteAll(actions: List<ScrapeAction>)
}
