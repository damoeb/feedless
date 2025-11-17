package org.migor.feedless.api

import org.migor.feedless.Vertical
import org.migor.feedless.generated.types.Vertical as VerticalDto

fun VerticalDto.fromDto(): Vertical {
  return when (this) {
    VerticalDto.all -> Vertical.all
    VerticalDto.visualDiff -> Vertical.visualDiff
//    org.migor.feedless.generated.types.ProductName.pageChangeTracker -> ProductName.pageChangeTracker
    VerticalDto.rssProxy -> Vertical.rssProxy
    VerticalDto.reader -> Vertical.reader
//    org.migor.feedless.generated.types.ProductName.upcoming -> ProductName.upcoming
//    org.migor.feedless.generated.types.ProductName.digest -> ProductName.digest
    VerticalDto.feedless -> Vertical.feedless
    VerticalDto.feedDump -> Vertical.feedDump
    VerticalDto.untoldNotes -> Vertical.untoldNotes
    VerticalDto.upcoming -> Vertical.upcoming
    else -> throw IllegalArgumentException("$this is not a valid product name")
  }
}

fun Vertical.toDto(): VerticalDto {
  return when (this) {
    Vertical.all -> VerticalDto.all
    Vertical.visualDiff -> VerticalDto.visualDiff
//    org.migor.feedless.generated.types.ProductName.pageChangeTracker -> ProductName.pageChangeTracker
    Vertical.rssProxy -> VerticalDto.rssProxy
    Vertical.reader -> VerticalDto.reader
//    org.migor.feedless.generated.types.ProductName.upcoming -> ProductName.upcoming
//    org.migor.feedless.generated.types.ProductName.digest -> ProductName.digest
    Vertical.feedless -> VerticalDto.feedless
    Vertical.feedDump -> VerticalDto.feedDump
    Vertical.untoldNotes -> VerticalDto.untoldNotes
    Vertical.upcoming -> VerticalDto.upcoming
    else -> throw IllegalArgumentException("$this is not a valid product name")
  }
}
