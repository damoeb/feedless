package org.migor.feedless.text.datetime

import java.time.LocalDateTime
import java.time.LocalTime

data class DateTimeCandidate(
  val input: String,
  val range: IntRange,
  val format: String,
  val dateTime: LocalDateTime,
  val hasTime: Boolean,
  val occurrences: Int = 1,
  val endsAt: LocalTime? = null,
)

data class TimeCandidate(
  val input: String,
  val range: IntRange,
  val time: LocalTime,
)

enum class DateTimeConfidence {
  high,
  medium,
  low,
  mismatch,
  none,
}

/** The time a date without one is given, so a startingAt at this time may be only a date. */
val DATE_ONLY_TIME: LocalTime = LocalTime.of(8, 0)

private const val MAX_LISTED_INPUTS = 5
private const val MAX_INPUT_LENGTH = 40

fun List<DateTimeCandidate>.rateAgainst(startingAt: LocalDateTime): DateTimeConfidence {
  val exact = filter { it.hasTime && it.dateTime == startingAt }
  val sameDay = filter { it.dateTime.toLocalDate() == startingAt.toLocalDate() }
  return when {
    isEmpty() -> DateTimeConfidence.none
    size == 1 && exact.isNotEmpty() -> DateTimeConfidence.high
    exact.isNotEmpty() -> DateTimeConfidence.medium
    // a start with an end time is the event's schedule, so it fills in a date-only startingAt or contradicts it
    sameDay.any { it.endsAt != null } ->
      if (startingAt.toLocalTime() == DATE_ONLY_TIME) DateTimeConfidence.medium else DateTimeConfidence.mismatch
    size == 1 && sameDay.isNotEmpty() && !single().hasTime -> DateTimeConfidence.medium
    sameDay.isNotEmpty() -> DateTimeConfidence.low
    else -> DateTimeConfidence.mismatch
  }
}

fun List<DateTimeCandidate>.summarize(startingAt: LocalDateTime): String {
  val noun = if (size == 1) "candidate" else "candidates"
  val inputs = take(MAX_LISTED_INPUTS).joinToString(", ") { "'${it.input.abbreviate()}'" }
  val more = if (size > MAX_LISTED_INPUTS) " +${size - MAX_LISTED_INPUTS} more" else ""
  val range = firstOrNull { it.endsAt != null && it.dateTime.toLocalDate() == startingAt.toLocalDate() }
    ?.let { ", range ${it.dateTime.toLocalTime()}-${it.endsAt}" }
    .orEmpty()
  return "$size datetime $noun [$inputs$more] vs startingAt $startingAt -> confidence ${rateAgainst(startingAt)}$range"
}

private fun String.abbreviate(): String =
  if (length > MAX_INPUT_LENGTH) "${take(MAX_INPUT_LENGTH)}…" else this
