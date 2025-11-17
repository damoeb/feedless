package org.migor.feedless.api

import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.source.ExtractEmit


fun ScrapeEmit.fromDto(): ExtractEmit {
  return when (this) {
    ScrapeEmit.text -> ExtractEmit.text
    ScrapeEmit.html -> ExtractEmit.html
    ScrapeEmit.pixel -> ExtractEmit.pixel
    ScrapeEmit.date -> ExtractEmit.date
  }
}

fun ExtractEmit.toDto(): ScrapeEmit {
  return when (this) {
    ExtractEmit.text -> ScrapeEmit.text
    ExtractEmit.html -> ScrapeEmit.html
    ExtractEmit.pixel -> ScrapeEmit.pixel
    ExtractEmit.date -> ScrapeEmit.date
  }
}
