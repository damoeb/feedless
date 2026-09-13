package org.migor.feedless

data class SortableRequest(val field: String, val asc: Boolean)

/** [limit] may exceed [pageSize] to detect hasMore, but [offset] always uses [pageSize], so page boundaries never move. */
data class PageableRequest(
  val pageNumber: Int,
  val pageSize: Int,
  val sortBy: List<SortableRequest> = emptyList(),
  val limit: Int = pageSize,
) {
  val offset: Int get() = pageNumber * pageSize

  companion object {
    /** Fetches one extra row to answer hasMore in a single request. */
    fun withExtraForHasMore(pageNumber: Int, pageSize: Int): PageableRequest =
      PageableRequest(pageNumber = pageNumber, pageSize = pageSize, limit = pageSize + 1)
  }
}
