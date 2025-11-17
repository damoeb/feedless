package org.migor.feedless.actions

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class HeaderAction(
  override val id: ScrapeActionId,
  override val pos: Int?,
  override val sourceId: SourceId,
  val name: String,
  val value: String,
  override val createdAt: LocalDateTime
) : ScrapeAction(id, pos, sourceId, createdAt)

