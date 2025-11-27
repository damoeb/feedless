package org.migor.feedless

data class SortableRequest(val field: String, val asc: Boolean)
data class PageableRequest(val pageNumber: Int, val pageSize: Int, val sortBy: List<SortableRequest> = emptyList())
