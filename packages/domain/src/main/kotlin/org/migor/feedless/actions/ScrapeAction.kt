package org.migor.feedless.actions

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime
import java.util.UUID

sealed class ScrapeAction(
    open val id: ScrapeActionId = ScrapeActionId(UUID.randomUUID()),
    open val pos: Int? = null,
    open val sourceId: SourceId,
    open val createdAt: LocalDateTime = LocalDateTime.now()
)

