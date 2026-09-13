package org.migor.feedless.data.jpa.harvest

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.data.jpa.repository.toPageRequest
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestId
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.harvest.HarvestStatus
import org.migor.feedless.source.SourceId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
class HarvestJpaRepository(private val harvestDAO: HarvestDAO) : HarvestRepository {
  override fun findAllBySourceId(
    sourceId: SourceId,
    dryRun: Boolean,
    pageable: PageableRequest
  ): List<Harvest> {
    return harvestDAO.findAllBySourceIdAndDryRun(sourceId.uuid, dryRun, pageable.toPageRequest())
      .map { it.toDomain() }
  }

  override fun findById(id: HarvestId): Harvest? {
    return harvestDAO.findById(id.uuid).getOrNull()?.toDomain()
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

  @Transactional
  override fun startRun(sourceId: SourceId, now: LocalDateTime): Harvest? {
    val id = UUID.randomUUID()
    return if (harvestDAO.insertRunningUnlessSourceRuns(id, sourceId.uuid, now) == 1) {
      harvestDAO.findById(id).getOrNull()?.toDomain()
    } else {
      null
    }
  }

  // One transaction: the row locks from the SELECT are held until the rows are committed as running.
  // Flushed here, so a refused claim surfaces from this call as a DataIntegrityViolationException.
  @Transactional
  override fun claimQueued(limit: Int, now: LocalDateTime): List<Harvest> {
    val claimed = harvestDAO.findQueuedForUpdateSkipLocked(limit)
    claimed.forEach {
      it.status = RUNNING
      it.startedAt = now
    }
    return harvestDAO.saveAllAndFlush(claimed).map { it.toDomain() }
  }

  @Transactional
  override fun completeStaleRunning(startedBefore: LocalDateTime, now: LocalDateTime, message: String): Int {
    return harvestDAO.completeAllRunningStartedBefore(startedBefore, now, message)
  }

  private companion object {
    // t_harvest.status values — see HarvestMapper.harvestStatusToString.
    val RUNNING = HarvestStatus.RUNNING.name.lowercase()
  }
}
