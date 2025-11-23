package org.migor.feedless.actions

import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.SourceId
import java.time.LocalDateTime
import java.util.*

data class ExtractXpathAction(
    override val id: ScrapeActionId = ScrapeActionId(UUID.randomUUID()),
    override val pos: Int? = null,
    override val sourceId: SourceId,
    val fragmentName: String,
    val xpath: String,
    val emit: Array<ExtractEmit>,
    val uniqueBy: ExtractEmit,
    override val createdAt: LocalDateTime = LocalDateTime.now()
) : ScrapeAction(id, pos, sourceId, createdAt) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExtractXpathAction

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

