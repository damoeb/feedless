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

private const val MAX_LISTED_INPUTS = 5
private const val MAX_INPUT_LENGTH = 40

fun List<DateTimeCandidate>.rateAgainst(startingAt: LocalDateTime): DateTimeConfidence {
  val exact = filter { it.hasTime && it.dateTime == startingAt }
  val sameDay = filter { it.dateTime.toLocalDate() == startingAt.toLocalDate() }
  return when {
    isEmpty() -> DateTimeConfidence.none
    size == 1 && exact.isNotEmpty() -> DateTimeConfidence.high
    exact.isNotEmpty() -> DateTimeConfidence.medium
    size == 1 && sameDay.isNotEmpty() && !single().hasTime -> DateTimeConfidence.medium
    sameDay.isNotEmpty() -> DateTimeConfidence.low
    else -> DateTimeConfidence.mismatch
  }
}

fun List<DateTimeCandidate>.summarize(startingAt: LocalDateTime): String {
  val noun = if (size == 1) "candidate" else "candidates"
  val inputs = take(MAX_LISTED_INPUTS).joinToString(", ") { "'${it.input.abbreviate()}'" }
  val more = if (size > MAX_LISTED_INPUTS) " +${size - MAX_LISTED_INPUTS} more" else ""
  return "$size datetime $noun [$inputs$more] vs startingAt $startingAt -> confidence ${rateAgainst(startingAt)}"
}

private fun String.abbreviate(): String =
  if (length > MAX_INPUT_LENGTH) "${take(MAX_INPUT_LENGTH)}…" else this
