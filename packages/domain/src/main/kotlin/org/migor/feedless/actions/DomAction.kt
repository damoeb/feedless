package org.migor.feedless.actions

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class DomAction(
  override val id: ScrapeActionId,
  override val pos: Int?,
  override val sourceId: SourceId,
  val xpath: String,
  val event: DomEventType,
  val data: String?,
  override val createdAt: LocalDateTime
) : ScrapeAction(id, pos, sourceId, createdAt)

