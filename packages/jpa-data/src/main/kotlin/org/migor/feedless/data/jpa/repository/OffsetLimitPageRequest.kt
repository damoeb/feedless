package org.migor.feedless.data.jpa.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/** A [Pageable] whose offset doesn't depend on its fetch size: PageRequest can't fetch S + 1 rows without shifting the offset. */
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
