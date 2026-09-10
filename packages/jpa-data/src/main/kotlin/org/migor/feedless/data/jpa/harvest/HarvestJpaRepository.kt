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
import java.time.LocalDateTime

@Component
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
class HarvestJpaRepository(private val harvestDAO: HarvestDAO) : HarvestRepository {
  override fun findAllBySourceId(
    sourceId: SourceId,
    dryRun: Boolean,
    pageable: PageableRequest
  ): List<Harvest> {
    return harvestDAO.findAllBySourceIdAndDryRunOrderByCreatedAtDesc(sourceId.uuid, dryRun, pageable.toPageRequest())
      .map { it.toDomain() }
  }

  override fun deleteAllTailingBySourceId() {
    harvestDAO.deleteAllTailingBySourceId()
  }

  override fun deleteAllDryRunByCreatedAtBefore(before: LocalDateTime) {
    harvestDAO.deleteAllByDryRunTrueAndStatusAndCreatedAtBefore("completed", before)
  }

  override fun save(harvest: Harvest): Harvest {
    return harvestDAO.save(harvest.toEntity()).toDomain()
  }
}
