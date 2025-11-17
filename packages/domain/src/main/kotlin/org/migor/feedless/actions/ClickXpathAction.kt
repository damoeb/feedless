package org.migor.feedless.actions

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class ClickXpathAction(
  override val id: ScrapeActionId,
  override val pos: Int?,
  override val sourceId: SourceId,
  val xpath: String,
  override val createdAt: LocalDateTime
) : ScrapeAction(id, pos, sourceId, createdAt)

