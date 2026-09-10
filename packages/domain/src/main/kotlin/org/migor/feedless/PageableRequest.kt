package org.migor.feedless

data class SortableRequest(val field: String, val asc: Boolean)

/**
 * [pageSize] is the true, requested page size: [offset] is always `pageNumber * pageSize`, so
 * page boundaries never move. [limit] is how many rows a query actually fetches — it defaults to
 * [pageSize], but a caller that wants one extra row to answer `hasMore` without a second request
 * sets it higher via [withExtraForHasMore] instead of inflating [pageSize] itself. Inflating
 * [pageSize] directly was T6's bug: it shifts [offset] too, and the query skips one row at every
 * page boundary.
 */
data class PageableRequest(
  val pageNumber: Int,
  val pageSize: Int,
  val sortBy: List<SortableRequest> = emptyList(),
  val limit: Int = pageSize,
) {
  /** Row offset for this page — based on the true [pageSize], never on [limit]. */
  val offset: Int get() = pageNumber * pageSize

  companion object {
    /**
     * A page of [pageSize] rows that additionally fetches one extra (`limit = pageSize + 1`), so
     * the caller can answer `hasMore` from a single request without shifting where the *next*
     * page starts. The standard "list" pagination contract across `/api/v1`.
     */
    fun withExtraForHasMore(pageNumber: Int, pageSize: Int): PageableRequest =
      PageableRequest(pageNumber = pageNumber, pageSize = pageSize, limit = pageSize + 1)
  }
}
