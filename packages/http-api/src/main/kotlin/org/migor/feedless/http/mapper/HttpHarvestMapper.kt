package org.migor.feedless.http.mapper

import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestStatus
import org.migor.feedless.util.toOffsetDateTime
import org.springframework.stereotype.Component
import org.migor.feedless.http.api.model.Harvest as HttpHarvest
import org.migor.feedless.http.api.model.HarvestStatus as HttpHarvestStatus

@Component
class HttpHarvestMapper {

  fun toHttp(harvest: Harvest): HttpHarvest {
    val completed = harvest.status == HarvestStatus.COMPLETED
    return HttpHarvest(
      id = harvest.id.uuid,
      sourceId = harvest.sourceId.uuid,
      status = toHttpStatus(harvest.status),
      dryRun = harvest.dryRun,
      startedAt = harvest.startedAt.toOffsetDateTime(),
      // Outcome fields are omitted until the harvest finishes, rather than a misleading zero/false.
      ok = if (completed) !harvest.errornous else null,
      itemsAdded = if (completed) harvest.itemsAdded else null,
      itemsIgnored = if (completed) harvest.itemsIgnored else null,
      finishedAt = if (completed) harvest.finishedAt?.toOffsetDateTime() else null,
    )
  }

  private fun toHttpStatus(status: HarvestStatus): HttpHarvestStatus = when (status) {
    HarvestStatus.QUEUED -> HttpHarvestStatus.queued
    HarvestStatus.RUNNING -> HttpHarvestStatus.running
    HarvestStatus.COMPLETED -> HttpHarvestStatus.completed
  }
}
