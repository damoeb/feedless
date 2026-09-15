package org.migor.feedless.document

interface DocumentQueryParser {
  /** JSON of the GraphQL RecordsWhereInput, as the feed URL's `where` parameter carries it. */
  fun parseFilter(json: String): DocumentsFilter

  /** JSON of the GraphQL RecordOrderByInput. */
  fun parseOrderBy(json: String): RecordOrderBy
}
