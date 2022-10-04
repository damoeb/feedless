package org.migor.rich.rss.api

import org.migor.rich.rss.harvest.HarvestException
import kotlin.time.toDuration

class HostOverloadingException(override val message: String, waitForRefill: Long) : RuntimeException() {
  var secondsForRefill: Long

  init {
    secondsForRefill = waitForRefill.toDuration(kotlin.time.DurationUnit.NANOSECONDS).inWholeSeconds
  }
}

class TemporaryServerException(override val message: String, waitForRefill: Long) : RuntimeException() {
  var secondsForRefill: Long

  init {
    secondsForRefill = waitForRefill.toDuration(kotlin.time.DurationUnit.NANOSECONDS).inWholeSeconds
  }
}
