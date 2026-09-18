package org.migor.feedless.text.datetime

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.scrape.LogCollector
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val MAX_RANGE_GAP = 12

@Service
class DateTimeExtractor {

  private val log = LoggerFactory.getLogger(DateTimeExtractor::class.simpleName)

  private val allMonth = (1..12).map {
    LocalDate.parse(
      "2018-${StringUtils.leftPad("$it", 2, '0')}-01",
      DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )
  }
  private val days = listOf(Pair("\\d{1}", "d"), Pair("\\d{2}", "dd"))
  private val months =
    listOf(Pair("\\d{1}", "M"), Pair("\\d{2}", "MM"), Pair("[a-z]{3}", "LLL"), Pair("[a-z]{3,}", "MMMM"))
  private val years = listOf(Pair("\\d{2}", "yy"), Pair("\\d{4}", "yyyy"))

  // credits https://stackoverflow.com/a/3390252
  private val dateFormatToRegexp = mutableListOf(
    Triple(toRegex("\\d{8}"), "yyyyMMdd", false),
    Triple(toRegex("\\d{1,2}\\s[a-z]{3}\\s\\d{4}"), "dd MMM yyyy", false),
    Triple(toRegex("\\d{1}\\s[a-z]{3,}\\s\\d{4}"), "d MMMM yyyy", false),
    Triple(toRegex("\\d{2}\\s[a-z]{3,}\\s\\d{4}"), "dd MMMM yyyy", false),
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
    return partA.flatMap { a ->
      partB.flatMap { b ->
        partC.map { c ->
          Triple(
            toRegex("${a.first}\\s${b.first}\\s${c.first}"),
            "${a.second} ${b.second} ${c.second}",
            false
          )
        }
      }
    }
  }

  private fun toRegex(regex: String): Regex {
    return Regex(regex, RegexOption.IGNORE_CASE)
  }

//  fun claimDateRangeFromString(corrId: String, dateRangeStr: String, locale: Locale): Date? {
//    return dateRangeSplitter
//      .asSequence()
//      .filter { dateRangeStr.contains(it) }
//      .map { dateRangeStr.split(it) }
//      .filter { it.size == 2 }
//      .map {=
//        run {
//          val fromDateStr = it[0]
//          val (format, dateString, hasTime) = guessDateFormats(corrId, fromDateStr).first()
//          val fromDate = applyDateFormat(dateString, locale, format, hasTime)
////            val toDate = applyDateFormat(toDateStr, locale, format, hasTime)
////            fromDate.rangeTo(toDate)
//          fromDate
//        }
//      }.firstOrNull()
//  }

  // H:mm, HH:mm, H.mm or HH.mm, not a piece of a longer number such as 1.10.3
  private val timePattern = Regex("(?<![\\p{N}.:])(\\d{1,2})[:.](\\d{2})(?![\\p{N}]|[.:]\\p{N})")

  // edges keep a format from matching inside a longer word or number
  private val candidateFormats by lazy {
    dateFormatToRegexp
      .sortedByDescending { it.second.length }
      .sortedByDescending { it.third }
      .map { (regex, format, hasTime) ->
        Triple(toRegex("(?<![\\p{L}\\p{N}])(?:${regex.pattern})(?![\\p{L}\\p{N}])"), format, hasTime)
      }
  }

  suspend fun extractCandidates(text: String, locale: Locale): List<DateTimeCandidate> {
    val candidates = findCandidates(text, locale)
    val times = standaloneTimes(text, candidates)
    return candidates
      .map { candidate -> candidate.copy(endsAt = endTimeOf(candidate, times)) }
      .groupBy { it.dateTime }
      .values
      .map { same -> (same.firstOrNull { it.endsAt != null } ?: same.first()).copy(occurrences = same.size) }
  }

  /** Times that are not part of a date, e.g. the end of an event. */
  suspend fun extractTimes(text: String, locale: Locale): List<TimeCandidate> {
    return standaloneTimes(text, findCandidates(text, locale))
  }

  // "19:30 bis 21:00 Uhr" or "19:30 Uhr - 21:00": the end follows the start closely
  private fun endTimeOf(candidate: DateTimeCandidate, times: List<TimeCandidate>): LocalTime? {
    if (!candidate.hasTime) {
      return null
    }
    return times.firstOrNull { it.range.first > candidate.range.last }
      ?.takeIf { it.range.first - candidate.range.last <= MAX_RANGE_GAP }
      ?.time
      ?.takeIf { it.isAfter(candidate.dateTime.toLocalTime()) }
  }

  private fun standaloneTimes(text: String, candidates: List<DateTimeCandidate>): List<TimeCandidate> {
    val dateRanges = candidates.map { it.range }
    return timePattern.findAll(text)
      .filter { match -> dateRanges.none { it.first <= match.range.last && match.range.first <= it.last } }
      .mapNotNull { match ->
        runCatching { LocalTime.of(match.groupValues[1].toInt(), match.groupValues[2].toInt()) }
          .getOrNull()
          ?.let { time -> TimeCandidate(match.value, match.range, time) }
      }
      .toList()
  }

  private suspend fun findCandidates(text: String, locale: Locale): List<DateTimeCandidate> {
    val (normalized, origin) = normalizeWithOrigin(text, locale)
    val stripped = StringUtils.stripAccents(normalized)
    val searchable = if (stripped.length == normalized.length) stripped else normalized

    val claimed = mutableListOf<IntRange>()
    val candidates = mutableListOf<DateTimeCandidate>()
    candidateFormats.forEach { (regex, format, hasTime) ->
      regex.findAll(searchable).forEach { match ->
        val span = match.range
        if (claimed.none { it.first <= span.last && span.first <= it.last }) {
          applyDateFormat(normalized.substring(span), locale, format, hasTime)?.let { dateTime ->
            claimed.add(span)
            val range = origin[span.first]..origin[span.last]
            val input = text.substring(range).replace("\\s+".toRegex(), " ")
            candidates.add(DateTimeCandidate(input, range, format, dateTime, hasTime))
          }
        }
      }
    }
    return candidates.sortedBy { it.range.first }
  }

  /** Mirrors the normalization of [extractDateTime], keeping each character's index in [text]. */
  private fun normalizeWithOrigin(text: String, locale: Locale): Pair<String, IntArray> {
    val monthChars = allMonth
      .joinToString("") { DateTimeFormatter.ofPattern("MMMM", locale).format(it) }
      .lowercase()
      .toSet()
    val normalized = StringBuilder()
    val origin = ArrayList<Int>()
    text.forEachIndexed { index, char ->
      val isDateTimeSeparator = char == 'T' && text.getOrNull(index - 1)?.isDigit() == true &&
        text.getOrNull(index + 1)?.isDigit() == true
      val keep = char == ':' || (char.isLetterOrDigit() && (char.code < 128 || char.lowercaseChar() in monthChars))
      if (keep && !isDateTimeSeparator) {
        normalized.append(char)
        origin.add(index)
      } else if (normalized.isNotEmpty() && normalized.last() != ' ') {
        normalized.append(' ')
        origin.add(index)
      }
    }
    return Pair(normalized.toString(), origin.toIntArray())
  }

  suspend fun extractDateTime(
    dateTimeStrParam: String,
    locale: Locale,
    logger: LogCollector
  ): LocalDateTime? {
    logger.log(" parsing '$dateTimeStrParam' locale=$locale")

    runCatching {
      val date = LocalDateTime.parse(dateTimeStrParam)
      logger.log("-> $date")
      return date
    }

    runCatching {
      val date = LocalDateTime.parse(dateTimeStrParam, DateTimeFormatter.ISO_DATE_TIME)
      logger.log("-> $date")
      return date
    }

    val relevantChars =
      allMonth.joinToString("") { DateTimeFormatter.ofPattern("MMMM", locale).format(it) }.lowercase().split("")
        .distinct().joinToString("")

    return runCatching {
      val simpleDateTimeStr = dateTimeStrParam
        .trim().replace(".", " ")
        .replace("\n", " ")
        .replace(
          "[^\\p{Alnum}$relevantChars:]".toRegex(RegexOption.IGNORE_CASE),
          " "
        )
        .replace("T", " ")
        .replace("\\s+".toRegex(), " ")
      val date = guessDateFormats(simpleDateTimeStr, logger)
        .firstNotNullOfOrNull { (format, dateString, hasTime) -> applyDateFormat(dateString, locale, format, hasTime) }
      logger.log("-> $date")
      date
//    }.onFailure {
//      runCatching {
//        return claimDateRangeFromString(corrId, dateTimeStrParam, locale)
//      }
//      log.error("Cannot parse dateString $dateTimeStrParam")
    }.getOrNull()
  }


  private suspend fun applyDateFormat(
    simpleDateTimeStr: String,
    locale: Locale,
    format: String,
    hasTime: Boolean
  ): LocalDateTime? {
    val formatter = DateTimeFormatter.ofPattern(format, locale)
    return try {
      if (hasTime) {
        LocalDateTime.parse(simpleDateTimeStr, formatter)
      } else {
        LocalDate.parse(simpleDateTimeStr, formatter).atTime(DATE_ONLY_TIME)
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
  private suspend fun guessDateFormats(
    dateString: String,
    logger: LogCollector
  ): List<Triple<String, String, Boolean>> {
    val normalizedDateString = StringUtils.stripAccents(dateString)
    logger.log("guessDateFormat for '$dateString' as '${normalizedDateString}")
    return dateFormatToRegexp
      .sortedByDescending { it.second.length }
      .sortedByDescending { it.third }
      .mapNotNullTo(ArrayList()) { (regex, dateFormat, hasTime) ->
        run {
          val matches = regex.find(normalizedDateString)
          val doesMatch = matches?.groups?.isEmpty() == false
          if (doesMatch) {
//            logger.log("satisfies $dateFormat")
            matches.groups[0]?.range?.let {
              Triple(dateFormat, dateString.substring(it), hasTime)
            }
          } else {
            null
          }
        }
      }
  }

}
