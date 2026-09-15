package org.migor.feedless.repository

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.util.toLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Longest log a harvest keeps; longer logs are cut with an ellipsis. */
const val HARVEST_LOG_MAX_LENGTH = 32000

private const val ELLIPSIS = "..."
private const val APPENDIX_SEPARATOR = "\n\n"

/** One harvest log line: an ISO timestamp, two spaces, the message. */
fun harvestLogLine(time: LocalDateTime, message: String): String =
  "${time.format(DateTimeFormatter.ISO_DATE_TIME)}  $message"

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
