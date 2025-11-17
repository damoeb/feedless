package org.migor.feedless.actions

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

sealed class ScrapeAction(
  open val id: ScrapeActionId,
  open val pos: Int?,
  open val sourceId: SourceId,
  open val createdAt: LocalDateTime
)

