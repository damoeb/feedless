package org.migor.feedless.actions

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime
import java.util.UUID

data class ExtractBoundingBoxAction(
    override val id: ScrapeActionId = ScrapeActionId(UUID.randomUUID()),
    override val pos: Int? = null,
    override val sourceId: SourceId,
    val x: Int,
    val fragmentName: String,
    val y: Int,
    val w: Int,
    val h: Int,
    override val createdAt: LocalDateTime = LocalDateTime.now()
) : ScrapeAction(id, pos, sourceId, createdAt)

