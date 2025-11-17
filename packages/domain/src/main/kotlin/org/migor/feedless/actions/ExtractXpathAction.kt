package org.migor.feedless.actions

import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class ExtractXpathAction(
  override val id: ScrapeActionId,
  override val pos: Int?,
  override val sourceId: SourceId,
  val fragmentName: String,
  val xpath: String,
  val emit: Array<ExtractEmit>,
  val emitRaw: Array<String>,
  val uniqueBy: ExtractEmit,
  override val createdAt: LocalDateTime
) : ScrapeAction(id, pos, sourceId, createdAt) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ExtractXpathAction

    if (id != other.id) return false
    if (!emitRaw.contentEquals(other.emitRaw)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + emitRaw.contentHashCode()
    return result
  }
}

