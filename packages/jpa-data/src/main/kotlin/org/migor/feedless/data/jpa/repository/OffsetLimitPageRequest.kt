package org.migor.feedless.data.jpa.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * A [Pageable] whose offset is independent of its fetch size. Spring Data's own [org.springframework.data.domain.PageRequest]
 * always computes `offset = pageNumber * pageSize`, which cannot express "page N of size S, but
 * fetch S + 1 rows this time" — the `hasMore`-without-a-second-request pattern used across
 * `/api/v1` list endpoints (see `PageableRequest.limit`). Without this, inflating the Spring Data
 * page size to fetch one extra row also inflates the offset, and every page after the first skips
 * one row.
 */
class OffsetLimitPageRequest(
  private val offsetValue: Long,
  private val limitValue: Int,
  private val sortValue: Sort,
) : Pageable {
  override fun getPageNumber(): Int = if (limitValue == 0) 0 else (offsetValue / limitValue).toInt()
  override fun getPageSize(): Int = limitValue
  override fun getOffset(): Long = offsetValue
  override fun getSort(): Sort = sortValue
  override fun next(): Pageable = OffsetLimitPageRequest(offsetValue + limitValue, limitValue, sortValue)
  override fun previousOrFirst(): Pageable =
    if (hasPrevious()) OffsetLimitPageRequest((offsetValue - limitValue).coerceAtLeast(0), limitValue, sortValue) else first()

  override fun first(): Pageable = OffsetLimitPageRequest(0, limitValue, sortValue)
  override fun hasPrevious(): Boolean = offsetValue > 0
  override fun withPage(pageNumber: Int): Pageable =
    OffsetLimitPageRequest(pageNumber.toLong() * limitValue, limitValue, sortValue)
}
