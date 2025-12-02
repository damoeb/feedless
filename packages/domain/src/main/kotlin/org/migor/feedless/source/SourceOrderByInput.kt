package org.migor.feedless.source

import org.migor.feedless.document.SortOrder

data class SourceOrderBy(
  val title: SortOrder? = null,
  val lastRecordsRetrieved: SortOrder? = null,
  val lastRefreshedAt: SortOrder? = null,
)
