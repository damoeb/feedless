package org.migor.feedless.data.jpa.source.actions

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
  override fun findAllBySourceId(id: SourceId): List<ScrapeAction> {
    return scrapeActionDAO.findAllBySourceId(id.uuid).map { it.toDomain() }
  }

  override fun saveAll(actions: List<ScrapeAction>): List<ScrapeAction> {
    return scrapeActionDAO.saveAll(actions.map { it.toEntity() }).map { it.toDomain() }
  }

  override fun deleteAll(actions: List<ScrapeAction>) {
    scrapeActionDAO.deleteAllById(actions.map { it.id.uuid })
  }

}
