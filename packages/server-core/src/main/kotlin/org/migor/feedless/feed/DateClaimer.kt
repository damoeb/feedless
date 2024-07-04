package org.migor.feedless.feed

import org.migor.feedless.common.PropertyService
import org.migor.feedless.util.toDate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Service
class DateClaimer(@Autowired private var propertyService: PropertyService) {

  private val log = LoggerFactory.getLogger(DateClaimer::class.simpleName)

  private val dateRangeSplitter = listOf(" - ")

  private val days = listOf(Pair("\\d{1}", "d"), Pair("\\d{2}", "dd"))
  private val months = listOf(Pair("\\d{1}", "M"), Pair("\\d{2}", "MM"), Pair("[a-z]{3}", "LLL"), Pair("[a-z]{4,}", "MMMM"))
  private val years = listOf(Pair("\\d{2}", "yy"), Pair("\\d{4}", "yyyy"))

  // credits https://stackoverflow.com/a/3390252
  private val dateFormatToRegexp = mutableListOf(
    Triple(toRegex("\\d{8}"), "yyyyMMdd", false),
    Triple(toRegex("\\d{1,2}\\s[a-z]{3}\\s\\d{4}"), "dd MMM yyyy", false),
    Triple(toRegex("\\d{1}\\s[a-z]{4,}\\s\\d{4}"), "d MMMM yyyy", false),
    Triple(toRegex("\\d{2}\\s[a-z]{4,}\\s\\d{4}"), "dd MMMM yyyy", false),
    Triple(toRegex("[a-z]{3,}\\s\\d{1}\\s\\d{4}"), "MMMM d yyyy", false), // December 8, 2020
    Triple(toRegex("[a-z]{3,}\\s\\d{2}\\s\\d{4}"), "MMMM dd yyyy", false), // December 15, 2020
    Triple(toRegex("\\d{12}"), "yyyyMMddHHmm", true),
    Triple(toRegex("\\d{8}\\s\\d{4}"), "yyyyMMdd HHmm", true),
    Triple(toRegex("\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}"), "dd MM yyyy HH:mm", true),
    Triple(toRegex("\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}"), "yyyy MM dd HH:mm", true),
    Triple(toRegex("\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}"), "MM dd yyyy HH:mm", true),
    Triple(toRegex("\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}"), "yyyy MM dd HH:mm", true),
    Triple(toRegex("\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}"), "dd MMM yyyy HH:mm", true),
    Triple(
      toRegex("\\d{1}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}"),
      "d MMMM yyyy HH:mm",
      true
    ), // 6. Januar 2022, 08:00 Uhr
    Triple(
      toRegex("\\d{2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}"),
      "dd MMMM yyyy HH:mm",
      true
    ), // 06. Januar 2022, 08:00 Uhr
    Triple(toRegex("\\d{14}"), "yyyyMMddHHmmss", true),
    Triple(toRegex("\\d{8}\\s\\d{6}"), "yyyyMMdd HHmmss", true),
    Triple(toRegex("\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}"), "dd MM yyyy HH:mm:ss", true),
    Triple(toRegex("\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}"), "yyyy MM dd HH:mm:ss", true),
    Triple(toRegex("\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}"), "MM dd yyyy HH:mm:ss", true),
    Triple(toRegex("\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}"), "yyyy MM dd HH:mm:ss", true),
    Triple(toRegex("\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}"), "dd MMM yyyy HH:mm:ss", true),
    Triple(toRegex("\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}"), "dd MMMM yyyy HH:mm:ss", true),
  )
  init {
    dateFormatToRegexp.addAll(permuate(days, months, years))
    dateFormatToRegexp.addAll(permuate(years, days, months))
    dateFormatToRegexp.addAll(permuate(years, months, days))
  }

  private fun permuate(
    partA: List<Pair<String, String>>,
    partB: List<Pair<String, String>>,
    partC: List<Pair<String, String>>
  ): List<Triple<Regex, String, Boolean>> {
    return partA.flatMap { a -> partB.flatMap { b -> partC.map { c -> Triple(toRegex("${a.first}\\s${b.first}\\s${c.first}"), "${a.second} ${b.second} ${c.second}", false) } } }
  }

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
          val (format, dateString, hasTime) = guessDateFormats(corrId, fromDateStr).first()!!
          val fromDate = applyDateFormat(dateString, locale, format, hasTime)
//            val toDate = applyDateFormat(toDateStr, locale, format, hasTime)
//            fromDate.rangeTo(toDate) // todo enable date range
          fromDate
        }
      }.firstOrNull()
  }

  fun claimDatesFromString(corrId: String, dateTimeStrParam: String, localeParam: Locale?): Date? {
    log.debug("[${corrId}] parsing $dateTimeStrParam")
    val locale = localeParam ?: propertyService.locale

    runCatching {
      val date = toDate(LocalDateTime.parse(dateTimeStrParam))
      log.debug("[${corrId}] -> $date")
      return date
    }

    runCatching {
      val date = toDate(LocalDateTime.parse(dateTimeStrParam, DateTimeFormatter.ISO_ZONED_DATE_TIME))
      log.debug("[${corrId}] -> $date")
      return date
    }

    runCatching {
      val date = toDate(DateTimeFormatter.ISO_DATE_TIME.parse(dateTimeStrParam))
      log.debug("[${corrId}] -> $date")
      return date
    }

//    runCatching {
//      val date = toDate(LocalDate.parse(dateTimeStrParam).atTime(8, 0))
//      log.debug("[${corrId}] -> $date")
//      return date
//    }

    return runCatching {
      val simpleDateTimeStr = dateTimeStrParam
        .trim().replace(".", " ")
        .replace("\n", " ")
        .replace("[^a-z0-9:]".toRegex(RegexOption.IGNORE_CASE), " ")
        .replace("T", " ")
        .replace("\\s+".toRegex(), " ")
      val date = guessDateFormats(corrId, simpleDateTimeStr)
        .firstNotNullOfOrNull { (format, dateString, hasTime) ->  applyDateFormat(dateString, locale, format, hasTime) }
      log.info("[${corrId}] -> $date")
      date
    }.onFailure {
      runCatching {
        return claimDateRangeFromString(corrId, dateTimeStrParam, locale)
      }
      log.error("[$corrId] Cannot parse dateString $dateTimeStrParam")
    }.getOrNull()
  }


  private fun applyDateFormat(simpleDateTimeStr: String, locale: Locale, format: String, hasTime: Boolean): Date? {
    val formatter = DateTimeFormatter.ofPattern(format, locale)
    return try {
      if (hasTime) {
        toDate(LocalDateTime.parse(simpleDateTimeStr, formatter))
      } else {
        toDate(LocalDate.parse(simpleDateTimeStr, formatter).atTime(8, 0))
      }
    } catch (e: Exception) {
      null
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
  private fun guessDateFormats(corrId: String, dateString: String): List<Triple<String, String, Boolean>> {
    log.info("[$corrId] guessDateFormat for '$dateString'")
    return dateFormatToRegexp
      .sortedByDescending { it.second.length }
      .sortedByDescending { it.third }
      .mapNotNullTo(ArrayList()) { (regex, dateFormat, hasTime) ->
        run {
          val matches = regex.find(dateString)
          val doesMatch = matches?.groups?.isEmpty() == false
          if (doesMatch) {
            log.info("[$corrId] satisfies $dateFormat")
            Triple(dateFormat, matches?.groups?.get(0)?.value!!, hasTime)
          } else {
            null
          }
        }
      }
  }

}
