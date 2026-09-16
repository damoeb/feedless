package org.migor.feedless.scrape

import org.migor.feedless.generated.types.ScrapeResponse

class AgentScrapeException(message: String) : RuntimeException(message)

fun ScrapeResponse.agentLogEntries(): List<LogEntry> = logs.map { LogEntry(message = "[agent] ${it.message}", time = it.time) }

fun ScrapeResponse.throwIfFailed() {
  if (!ok) {
    throw AgentScrapeException(errorMessage?.takeIf { it.isNotBlank() }?.let { "agent failed: $it" } ?: "agent failed without giving a reason")
  }
}
