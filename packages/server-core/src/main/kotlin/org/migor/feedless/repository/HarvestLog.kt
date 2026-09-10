package org.migor.feedless.repository

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.util.toLocalDateTime
import java.time.format.DateTimeFormatter

/** Longest log a harvest keeps; longer logs are cut with an ellipsis. */
internal const val HARVEST_LOG_MAX_LENGTH = 32000

/** The collected statements as a harvest log: one timestamped line each, capped at [HARVEST_LOG_MAX_LENGTH]. */
internal fun LogCollector.toHarvestLog(): String =
  StringUtils.abbreviate(
    logs.joinToString("\n") {
      "${it.time.toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME)}  ${it.message}"
    },
    "...",
    HARVEST_LOG_MAX_LENGTH,
  )
