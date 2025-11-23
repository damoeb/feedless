package org.migor.feedless.harvest

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class Harvest(
    val id: HarvestId = HarvestId(),
    val errornous: Boolean = false,
    val itemsAdded: Int = 0,
    val itemsIgnored: Int = 0,
    val logs: String,
    val startedAt: LocalDateTime,
    val finishedAt: LocalDateTime?,
    val sourceId: SourceId,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

