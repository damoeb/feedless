package org.migor.feedless.actions

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class ExtractBoundingBoxAction(
  override val id: ScrapeActionId,
  override val pos: Int?,
  override val sourceId: SourceId,
  val x: Int,
  val fragmentName: String,
  val y: Int,
  val w: Int,
  val h: Int,
  override val createdAt: LocalDateTime
) : ScrapeAction(id, pos, sourceId, createdAt)

