package org.migor.rss.rich.filter

class IncludeEntry(
  fieldName: String,
  operator: FilterOperator,
  value: String,
): EntryFilter(true, fieldName, operator, value)
