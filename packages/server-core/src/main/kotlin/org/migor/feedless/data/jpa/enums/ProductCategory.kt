package org.migor.feedless.data.jpa.enums

import org.migor.feedless.generated.types.ProductCategory as ProductCategoryDto

enum class ProductCategory {
  all,
  visualDiff,
  pageChangeTracker,
  rssProxy,
  reader,
  upcoming,
  digest,
  feedless,
  feedDump,
  untoldNotes,
}

fun ProductCategoryDto.fromDto(): ProductCategory {
  return when (this) {
    ProductCategoryDto.all -> ProductCategory.all
    ProductCategoryDto.visualDiff -> ProductCategory.visualDiff
//    org.migor.feedless.generated.types.ProductName.pageChangeTracker -> ProductName.pageChangeTracker
    ProductCategoryDto.rssProxy -> ProductCategory.rssProxy
    ProductCategoryDto.reader -> ProductCategory.reader
//    org.migor.feedless.generated.types.ProductName.upcoming -> ProductName.upcoming
//    org.migor.feedless.generated.types.ProductName.digest -> ProductName.digest
    ProductCategoryDto.feedless -> ProductCategory.feedless
    ProductCategoryDto.feedDump -> ProductCategory.feedDump
    ProductCategoryDto.untoldNotes -> ProductCategory.untoldNotes
    ProductCategoryDto.upcoming -> ProductCategory.upcoming
    else -> throw IllegalArgumentException("$this is not a valid product name")
  }
}

fun ProductCategory.toDto(): ProductCategoryDto {
  return when (this) {
    ProductCategory.all -> ProductCategoryDto.all
    ProductCategory.visualDiff -> ProductCategoryDto.visualDiff
//    org.migor.feedless.generated.types.ProductName.pageChangeTracker -> ProductName.pageChangeTracker
    ProductCategory.rssProxy -> ProductCategoryDto.rssProxy
    ProductCategory.reader -> ProductCategoryDto.reader
//    org.migor.feedless.generated.types.ProductName.upcoming -> ProductName.upcoming
//    org.migor.feedless.generated.types.ProductName.digest -> ProductName.digest
    ProductCategory.feedless -> ProductCategoryDto.feedless
    ProductCategory.feedDump -> ProductCategoryDto.feedDump
    ProductCategory.untoldNotes -> ProductCategoryDto.untoldNotes
    ProductCategory.upcoming -> ProductCategoryDto.upcoming
    else -> throw IllegalArgumentException("$this is not a valid product name")
  }
}
