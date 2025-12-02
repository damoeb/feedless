package org.migor.feedless.data.jpa.source.actions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.data.jpa.source.toDomain
import org.migor.feedless.data.jpa.source.toEntity
import org.migor.feedless.source.ScrapeActionRepository
import org.migor.feedless.source.SourceId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.scrape} & ${AppLayer.repository}")
class ScrapeActionJpaRepository(private val scrapeActionDAO: ScrapeActionDAO) : ScrapeActionRepository {
  override suspend fun findAllBySourceId(id: SourceId): List<ScrapeAction> {
    return withContext(Dispatchers.IO) {
      scrapeActionDAO.findAllBySourceId(id.uuid).map { it.toDomain() }
    }
  }

  override suspend fun saveAll(actions: List<ScrapeAction>) {
    return withContext(Dispatchers.IO) {
      scrapeActionDAO.saveAll(actions.map { it.toEntity() }).map { it.toDomain() }
    }
  }

  override suspend fun deleteAll(actions: List<ScrapeAction>) {
    withContext(Dispatchers.IO) {
      scrapeActionDAO.deleteAllById(actions.map { it.id.uuid })
    }
  }

}
