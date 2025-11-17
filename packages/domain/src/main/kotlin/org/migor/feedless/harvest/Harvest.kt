package org.migor.feedless.harvest

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class Harvest(
  val id: HarvestId,
  val errornous: Boolean,
  val itemsAdded: Int,
  val itemsIgnored: Int,
  val logs: String,
  val startedAt: LocalDateTime,
  val finishedAt: LocalDateTime?,
  val sourceId: SourceId,
  val createdAt: LocalDateTime
)

