package org.migor.feedless.source

import org.migor.feedless.actions.ScrapeAction

interface ScrapeActionRepository {
  fun findAllBySourceId(id: SourceId): List<ScrapeAction>
  fun saveAll(actions: List<ScrapeAction>): List<ScrapeAction>
  fun deleteAll(actions: List<ScrapeAction>)
}
