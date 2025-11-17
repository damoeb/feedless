package org.migor.feedless.actions

import org.migor.feedless.source.PuppeteerWaitUntil
import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class FetchAction(
  override val id: ScrapeActionId,
  override val pos: Int?,
  override val sourceId: SourceId,
  val timeout: Int?,
  val url: String,
  val language: String?,
  val forcePrerender: Boolean,
  val isVariable: Boolean,
  val viewportWidth: Int,
  val viewportHeight: Int,
  val isMobile: Boolean,
  val isLandscape: Boolean,
  val waitUntil: PuppeteerWaitUntil?,
  val additionalWaitSec: Int?,
  override val createdAt: LocalDateTime
) : ScrapeAction(id, pos, sourceId, createdAt)

