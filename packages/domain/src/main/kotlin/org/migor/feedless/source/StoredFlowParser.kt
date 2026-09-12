package org.migor.feedless.source

import org.migor.feedless.actions.ScrapeAction

interface StoredFlowParser {
  fun storedFlowToDomainActions(storedFlow: String): List<ScrapeAction>
}
