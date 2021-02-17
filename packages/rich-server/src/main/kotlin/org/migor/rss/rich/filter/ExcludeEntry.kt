package org.migor.rss.rich.filter

class ExcludeEntry(
  fieldName: String,
  operator: FilterOperator,
  value: String,
): EntryFilter(false, fieldName, operator, value)
