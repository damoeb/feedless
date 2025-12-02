package org.migor.feedless.data.jpa.harvest

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.data.jpa.repository.toPageRequest
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.source.SourceId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
class HarvestJpaRepository(private val harvestDAO: HarvestDAO) : HarvestRepository {
  override fun findAllBySourceId(
    sourceId: SourceId,
    pageable: PageableRequest
  ): List<Harvest> {
    return harvestDAO.findAllBySourceId(sourceId.uuid, pageable.toPageRequest()).map { it.toDomain() }
  }

  override fun deleteAllTailingBySourceId() {
    harvestDAO.deleteAllTailingBySourceId()
  }

  override fun save(harvest: Harvest): Harvest {
    return harvestDAO.save(harvest.toEntity()).toDomain()
  }
}
