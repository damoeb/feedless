package org.migor.feedless.scrape

import org.migor.feedless.util.toMillis
import java.time.LocalDateTime

class LogCollector {
  val logs = mutableListOf<LogEntry>()
  fun log(message: String) {
    logs.add(LogEntry(message = message, time = LocalDateTime.now().toMillis()))
  }
}

data class LogEntry(val message: String, val time: Long)
