package org.migor.rss.rich.filter

open class EntryFilter(
  var include: Boolean,
  var fieldName: String,
  var operator: FilterOperator,
  var value: String,
)
