package org.migor.feedless.data.jpa.enums

import org.migor.feedless.generated.types.ProductCategory as ProductCategoryDto

enum class ProductCategory {
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
    ProductCategoryDto.visualDiff -> ProductCategory.visualDiff
//    org.migor.feedless.generated.types.ProductName.pageChangeTracker -> ProductName.pageChangeTracker
    ProductCategoryDto.rssProxy -> ProductCategory.rssProxy
    ProductCategoryDto.reader -> ProductCategory.reader
//    org.migor.feedless.generated.types.ProductName.upcoming -> ProductName.upcoming
//    org.migor.feedless.generated.types.ProductName.digest -> ProductName.digest
    ProductCategoryDto.feedless -> ProductCategory.feedless
    ProductCategoryDto.feedDump -> ProductCategory.feedDump
    ProductCategoryDto.untoldNotes -> ProductCategory.untoldNotes
    else -> throw IllegalArgumentException("$this is not a valid product name")
  }
}

fun ProductCategory.toDto(): ProductCategoryDto {
  return when (this) {
    ProductCategory.visualDiff -> ProductCategoryDto.visualDiff
//    org.migor.feedless.generated.types.ProductName.pageChangeTracker -> ProductName.pageChangeTracker
    ProductCategory.rssProxy -> ProductCategoryDto.rssProxy
    ProductCategory.reader -> ProductCategoryDto.reader
//    org.migor.feedless.generated.types.ProductName.upcoming -> ProductName.upcoming
//    org.migor.feedless.generated.types.ProductName.digest -> ProductName.digest
    ProductCategory.feedless -> ProductCategoryDto.feedless
    ProductCategory.feedDump -> ProductCategoryDto.feedDump
    ProductCategory.untoldNotes -> ProductCategoryDto.untoldNotes
    else -> throw IllegalArgumentException("$this is not a valid product name")
  }
}
