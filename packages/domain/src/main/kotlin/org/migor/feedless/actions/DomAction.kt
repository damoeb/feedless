package org.migor.feedless.actions

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime
import java.util.UUID

data class DomAction(
    override val id: ScrapeActionId = ScrapeActionId(UUID.randomUUID()),
    override val pos: Int? = null,
    override val sourceId: SourceId,
    val xpath: String,
    val event: DomEventType,
    val data: String? = null,
    override val createdAt: LocalDateTime = LocalDateTime.now()
) : ScrapeAction(id, pos, sourceId, createdAt)

