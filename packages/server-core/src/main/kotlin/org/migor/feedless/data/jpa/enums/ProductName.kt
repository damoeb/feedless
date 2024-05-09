package org.migor.feedless.data.jpa.enums

enum class ProductName {
  visualDiff,
  pageChangeTracker,
  rssBuilder,
  reader,
  upcoming,
  digest,
  feedless,
  untoldNotes,
  system
}

fun org.migor.feedless.generated.types.ProductName.fromDto(): ProductName {
  return when (this) {
    org.migor.feedless.generated.types.ProductName.visualDiff -> ProductName.visualDiff
    org.migor.feedless.generated.types.ProductName.pageChangeTracker -> ProductName.pageChangeTracker
    org.migor.feedless.generated.types.ProductName.rssBuilder -> ProductName.rssBuilder
    org.migor.feedless.generated.types.ProductName.reader -> ProductName.reader
    org.migor.feedless.generated.types.ProductName.upcoming -> ProductName.upcoming
    org.migor.feedless.generated.types.ProductName.digest -> ProductName.digest
    org.migor.feedless.generated.types.ProductName.feedless -> ProductName.feedless
    org.migor.feedless.generated.types.ProductName.untoldNotes -> ProductName.untoldNotes
    else -> throw IllegalArgumentException()
  }
}
