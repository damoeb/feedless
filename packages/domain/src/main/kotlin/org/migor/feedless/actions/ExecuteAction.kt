package org.migor.feedless.actions

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class ExecuteAction(
  override val id: ScrapeActionId,
  override val pos: Int?,
  override val sourceId: SourceId,
  val pluginId: String,
  val executorParams: PluginExecutionJson?,
  override val createdAt: LocalDateTime
) : ScrapeAction(id, pos, sourceId, createdAt)

