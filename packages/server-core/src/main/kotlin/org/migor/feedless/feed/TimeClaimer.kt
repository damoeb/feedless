package org.migor.feedless.feed

import org.migor.feedless.scrape.LogCollector
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


@Service
@Transactional(propagation = Propagation.NEVER)
class TimeClaimer {

  private val log = LoggerFactory.getLogger(TimeClaimer::class.simpleName)

  // credits https://stackoverflow.com/a/3390252
  private val timeFormatToRegexp = mutableListOf(
    Pair(toRegex("\\d{2}:\\d{2}"), "HH:mm"),
    Pair(toRegex("[^0-9]|^\\d{1}:\\d{2}"), "H:mm"),
    Pair(toRegex("\\d{2}.\\d{2}"), "HH.mm"),
    Pair(toRegex("([^0-9]|^)\\d{1}.\\d{2}"), "H.mm"),
  )

  private fun toRegex(regex: String): Regex {
    return Regex(regex, RegexOption.IGNORE_CASE)
  }

  suspend fun claimTimeFromString(
    value: String,
    logger: LogCollector
  ): List<Pair<Int, LocalTime>> {
    logger.log("parsing '$value'")

    return run {
      val time = testTimeFormats(value, logger)
        .flatMap { (format, timeMatches) -> applyTimeFormat(timeMatches, format) }
        .map { Pair(it.first.first, it.second) }
        .sortedBy { it.first }
      logger.log("-> $time")
      time
    }
  }


  private suspend fun applyTimeFormat(
    timeMatches: List<MatchGroup>,
    format: String,
  ): List<Pair<IntRange, LocalTime>> {
    val formatter = DateTimeFormatter.ofPattern(format)
    return timeMatches.mapNotNull { matchGroup ->
      try {
        Pair(matchGroup.range, LocalTime.parse(matchGroup.value, formatter))
      } catch (e: Exception) {
        null
      }
    }
  }

  private suspend fun testTimeFormats(
    value: String,
    logger: LogCollector
  ): List<Pair<String, List<MatchGroup>>> {
    logger.log("testTimeFormats for '$value'")
    return timeFormatToRegexp
      .sortedByDescending { it.second.length }
      .mapTo(ArrayList()) { (regex, timeFormat) ->
        Pair(timeFormat, regex.findAll(value).map { it.groups[0] }.filterNotNull().toList())
  }.filter { it.second.isNotEmpty() }
  }
}
