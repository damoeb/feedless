package org.migor.feedless.actions

import org.migor.feedless.source.SourceId

fun ScrapeAction.placedAt(sourceId: SourceId, pos: Int): ScrapeAction =
  when (this) {
    is ClickPositionAction -> copy(sourceId = sourceId, pos = pos)
    is ClickXpathAction -> copy(sourceId = sourceId, pos = pos)
    is DomAction -> copy(sourceId = sourceId, pos = pos)
    is ExecuteAction -> copy(sourceId = sourceId, pos = pos)
    is ExtractBoundingBoxAction -> copy(sourceId = sourceId, pos = pos)
    is ExtractXpathAction -> copy(sourceId = sourceId, pos = pos)
    // The JPA round trip this replaces never kept these flags, so neither does this.
    is FetchAction -> copy(sourceId = sourceId, pos = pos, isVariable = false, isMobile = false, isLandscape = false)
    is HeaderAction -> copy(sourceId = sourceId, pos = pos)
    is WaitAction -> copy(sourceId = sourceId, pos = pos)
  }
