package org.migor.feedless.data.jpa.enums

enum class Product {
  visualDiff,
  pageChangeTracker,
  rssBuilder,
  reader,
  upcoming,
  digest,
  feedless,
  internal
}

fun org.migor.feedless.generated.types.Product.fromDto(): Product {
  return when(this) {
    org.migor.feedless.generated.types.Product.visualDiff -> Product.visualDiff
    org.migor.feedless.generated.types.Product.pageChangeTracker -> Product.pageChangeTracker
    org.migor.feedless.generated.types.Product.rssBuilder -> Product.rssBuilder
    org.migor.feedless.generated.types.Product.reader -> Product.reader
    org.migor.feedless.generated.types.Product.upcoming -> Product.upcoming
    org.migor.feedless.generated.types.Product.digest -> Product.digest
    org.migor.feedless.generated.types.Product.feedless -> Product.feedless
  }
}
