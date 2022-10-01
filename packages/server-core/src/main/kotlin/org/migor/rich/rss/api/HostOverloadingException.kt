package org.migor.rich.rss.api

import kotlin.time.toDuration

class HostOverloadingException(override val message: String, waitForRefill: Long) : RuntimeException() {
  var secondsForRefill: Long

  init {
    secondsForRefill = waitForRefill.toDuration(kotlin.time.DurationUnit.NANOSECONDS).inWholeSeconds
  }
}
