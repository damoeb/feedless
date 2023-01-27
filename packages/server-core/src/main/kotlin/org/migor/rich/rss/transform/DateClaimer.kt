package org.migor.rich.rss.transform

import org.migor.rich.rss.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


@Service
class DateClaimer(@Autowired private var propertyService: PropertyService) {

  private val log = LoggerFactory.getLogger(DateClaimer::class.simpleName)

  private val dateRangeSplitter = listOf(
    " - "
  )

  // credits https://stackoverflow.com/a/3390252
  private val dateFormatToRegexp = listOf(
    Triple(toRegex("^\\d{8}$"), "yyyyMMdd", false),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}$"), "dd MM yyyy", false),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}$"), "yyyy MM dd", false),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}$"), "MM dd yyyy", false),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}$"), "yyyy MM dd", false),
    Triple(toRegex("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$"), "dd MMM yyyy", false),
    Triple(toRegex("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$"), "dd MMMM yyyy", false),
    Triple(toRegex("^[a-z]{3,}\\s\\d{1}\\s\\d{4}$"), "MMMM d yyyy", false), // December 8, 2020
    Triple(toRegex("^[a-z]{3,}\\s\\d{2}\\s\\d{4}$"), "MMMM dd yyyy", false), // December 15, 2020
    Triple(toRegex("^\\d{12}$"), "yyyyMMddHHmm", true),
    Triple(toRegex("^\\d{8}\\s\\d{4}$"), "yyyyMMdd HHmm", true),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}$"), "dd MM yyyy HH:mm", true),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}$"), "yyyy MM dd HH:mm", true),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}$"), "MM dd yyyy HH:mm", true),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}$"), "yyyy MM dd HH:mm", true),
    Triple(toRegex("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$"), "dd MMM yyyy HH:mm", true),
    Triple(
      toRegex("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$"),
      "dd MMMM yyyy HH:mm",
      true
    ), // 06. Januar 2022, 08:00 Uhr
    Triple(toRegex("^\\d{14}$"), "yyyyMMddHHmmss", true),
    Triple(toRegex("^\\d{8}\\s\\d{6}$"), "yyyyMMdd HHmmss", true),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "dd MM yyyy HH:mm:ss", true),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "yyyy MM dd HH:mm:ss", true),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "MM dd yyyy HH:mm:ss", true),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "yyyy MM dd HH:mm:ss", true),
    Triple(toRegex("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "dd MMM yyyy HH:mm:ss", true),
    Triple(toRegex("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "dd MMMM yyyy HH:mm:ss", true),

    // Sonntag, 27. November
  )

  private fun toRegex(regex: String): Regex {
    return Regex(regex, RegexOption.IGNORE_CASE)
  }
  fun claimDateRangeFromString(corrId: String, dateRangeStr: String, locale: Locale): Date? {
    return dateRangeSplitter
        .asSequence()
        .filter { dateRangeStr.contains(it) }
        .map { dateRangeStr.split(it) }
        .filter { it.size == 2 }
        .map {
          run {
            val fromDateStr = it[0]
            val toDateStr = it[1]
            val (format, hasTime) = guessDateFormat(fromDateStr)!!
            val fromDate = applyDateFormat(fromDateStr, locale, format, hasTime)
//            val toDate = applyDateFormat(toDateStr, locale, format, hasTime)
//            fromDate.rangeTo(toDate) // todo enable date range
            fromDate
          }
        }.firstOrNull()
  }

  fun claimDatesFromString(corrId: String, dateTimeStrParam: String, localeParam: Locale?): Date? {
    log.info("[${corrId}] parsing $dateTimeStrParam")
    val dateTimeStr = dateTimeStrParam
      .trim().replace(".", " ")
    val locale = Optional.ofNullable(localeParam).orElse(propertyService.locale)!!
    return claimDatesOrDateRangeFromString(corrId, dateTimeStr, locale)
  }

  private fun claimDatesOrDateRangeFromString(corrId: String, dateTimeStr: String, locale: Locale): Date? {
    runCatching {
      val date = toDate(LocalDateTime.parse(dateTimeStr))
      log.debug("[${corrId}] -> $date")
      return date
    }

    runCatching {
      val date = toDate(LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_ZONED_DATE_TIME))
      log.debug("[${corrId}] -> $date")
      return date
    }

    runCatching {
      val date = toDate(LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
      log.debug("[${corrId}] -> $date")
      return date
    }

    runCatching {
      val date = toDate(LocalDate.parse(dateTimeStr).atTime(8, 0))
      log.debug("[${corrId}] -> $date")
      return date
    }

    return runCatching {
      val simpleDateTimeStr = dateTimeStr
        .replace("[^a-z0-9: ]".toRegex(RegexOption.IGNORE_CASE), "")
        .replace("\\s+".toRegex(), " ")
      val (format, hasTime) = guessDateFormat(simpleDateTimeStr)!!
      val date = applyDateFormat(simpleDateTimeStr, locale, format, hasTime)
      log.debug("[${corrId}] -> $date")
      date
    }.onFailure {

      runCatching {
        return claimDateRangeFromString(corrId, dateTimeStr, locale)
      }

      log.error("Cannot parse dateString $dateTimeStr")
    }.getOrNull()
  }

  private fun toDate(dt: LocalDateTime): Date {
    return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant())
  }

  private fun applyDateFormat(simpleDateTimeStr: String, locale: Locale, format: String, hasTime: Boolean): Date {
    val formatter = DateTimeFormatter.ofPattern(format, locale)
    return if (hasTime) {
      toDate(LocalDateTime.parse(simpleDateTimeStr, formatter))
    } else {
      toDate(LocalDate.parse(simpleDateTimeStr, formatter).atTime(8, 0))
    }
  }

  // credits https://stackoverflow.com/a/3390252
  /**
   * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
   * format is unknown. You can simply extend DateUtil with more formats if needed.
   * @param dateString The date string to determine the SimpleDateFormat pattern for.
   * @return The matching SimpleDateFormat pattern, or null if format is unknown.
   * @see SimpleDateFormat
   */
  private fun guessDateFormat(dateString: String): Pair<String, Boolean>? {
    return dateFormatToRegexp
      .filter { (regex, dateFormat, _) ->
        run {
          val matches = regex.matches(dateString)
          if (matches) {
            log.debug("$dateString looks like $dateFormat")
          }
          matches
        }
      }
      .map { (_, dateFormat, hasTime) -> Pair(dateFormat, hasTime) }
      .firstOrNull()
  }

}
