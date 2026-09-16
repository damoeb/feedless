package org.migor.feedless.repository

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.document.Document
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.util.toLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Longest log a harvest keeps; longer logs are cut with an ellipsis. */
const val HARVEST_LOG_MAX_LENGTH = 32000

private const val ELLIPSIS = "..."
private const val APPENDIX_SEPARATOR = "\n\n"

private const val MAX_LISTED_ITEMS = 5

// Fixed millis, as ISO_DATE_TIME trims trailing zeros and keeps nanos.
private val LINE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

/** One harvest log line: an ISO timestamp, two spaces, the message. */
fun harvestLogLine(time: LocalDateTime, message: String): String =
  "${time.format(LINE_TIME_FORMAT)}  $message"

/** The message, or the type when there is none, so a failure never logs as blank. */
fun Throwable.describe(): String =
  message?.takeIf { it.isNotBlank() } ?: this::class.simpleName ?: "unknown error"

/** How the harvest log names a document: its url, else its title, as items without link exist. */
fun Document.harvestLabel(): String = when {
  url.isNotBlank() -> url
  !title.isNullOrBlank() -> "'$title'"
  else -> "(no url, no title)"
}

fun importSummary(retrieved: Int, took: Long, new: Int, existing: List<String>): String {
  val listed = if (existing.isEmpty()) {
    ""
  } else {
    val more = existing.size - MAX_LISTED_ITEMS
    val names = existing.take(MAX_LISTED_ITEMS) + if (more > 0) listOf("+$more more") else emptyList()
    " (${names.joinToString(", ")})"
  }
  return "imported $retrieved items in ${took}ms: $new new, ${existing.size} existing$listed"
}

/** When it doesn't fit, the log gives way before the [appendix] (what the reader came for), which keeps up to half the limit. */
fun LogCollector.toHarvestLog(appendix: String? = null): String {
  val log = logs.joinToString("\n") { harvestLogLine(it.time.toLocalDateTime(), it.message) }
  if (appendix.isNullOrEmpty()) {
    return StringUtils.abbreviate(log, ELLIPSIS, HARVEST_LOG_MAX_LENGTH)
  }
  val cutAppendix = StringUtils.abbreviate(appendix, ELLIPSIS, HARVEST_LOG_MAX_LENGTH / 2)
  if (log.isEmpty()) {
    return cutAppendix
  }
  val logBudget = HARVEST_LOG_MAX_LENGTH - APPENDIX_SEPARATOR.length - cutAppendix.length
  return StringUtils.abbreviate(log, ELLIPSIS, logBudget) + APPENDIX_SEPARATOR + cutAppendix
}

/** [log] with [line] appended, capped at [HARVEST_LOG_MAX_LENGTH]. */
fun appendHarvestLog(log: String, line: String): String =
  StringUtils.abbreviate(
    if (log.isEmpty()) line else "$log\n$line",
    ELLIPSIS,
    HARVEST_LOG_MAX_LENGTH,
  )
