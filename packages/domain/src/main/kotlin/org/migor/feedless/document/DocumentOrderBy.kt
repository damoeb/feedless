package org.migor.feedless.document

data class RecordOrderBy(
  val startedAt: SortOrder? = null,
)

enum class SortOrder {
  ASC,
  DESC
}

