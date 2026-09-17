package org.migor.feedless.feed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.util.stream.Stream

internal class DateTimeConfidenceTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  fun rateAgainst(description: String, candidates: List<DateTimeCandidate>, expected: DateTimeConfidence) {
    assertThat(candidates.rateAgainst(startingAt)).isEqualTo(expected)
  }

  @Test
  fun `summary lists inputs, cuts long ones and counts the rest`() {
    val candidates = (1..7).map { candidate("${it}.09.2024 in a very long sentence that goes on and on", it, false) }

    assertThat(candidates.summarize(startingAt)).isEqualTo(
      "7 datetime candidates ['1.09.2024 in a very long sentence that g…', '2.09.2024 in a very long sentence that g…', " +
        "'3.09.2024 in a very long sentence that g…', '4.09.2024 in a very long sentence that g…', " +
        "'5.09.2024 in a very long sentence that g…' +2 more] vs startingAt 2024-09-27T20:15 -> confidence mismatch"
    )
  }

  @Test
  fun `summary uses the singular for one candidate`() {
    assertThat(listOf(candidate("27.09.2024 20:15", 27, true, 20, 15)).summarize(startingAt))
      .isEqualTo("1 datetime candidate ['27.09.2024 20:15'] vs startingAt 2024-09-27T20:15 -> confidence high")
  }

  companion object {
    private val startingAt = LocalDateTime.of(2024, 9, 27, 20, 15)

    private fun candidate(input: String, day: Int, hasTime: Boolean, hour: Int = 8, minute: Int = 0) =
      DateTimeCandidate(
        input = input,
        range = 0..input.length - 1,
        format = "",
        dateTime = LocalDateTime.of(2024, 9, day, hour, minute),
        hasTime = hasTime,
      )

    private val exact = candidate("27.09.2024 20:15", 27, true, 20, 15)
    private val sameDayNoTime = candidate("27.09.2024", 27, false)
    private val sameDayOtherTime = candidate("27.09.2024 18:00", 27, true, 18, 0)
    private val otherDay = candidate("12.09.2024", 12, false)

    @JvmStatic
    fun cases(): Stream<Arguments> = Stream.of(
      Arguments.of("only candidate equals startingAt", listOf(exact), DateTimeConfidence.high),
      Arguments.of("exact among others", listOf(exact, otherDay), DateTimeConfidence.medium),
      Arguments.of("only candidate, same day without time", listOf(sameDayNoTime), DateTimeConfidence.medium),
      Arguments.of("same day without time among others", listOf(sameDayNoTime, otherDay), DateTimeConfidence.low),
      Arguments.of("same day, other time", listOf(sameDayOtherTime), DateTimeConfidence.low),
      Arguments.of("no candidate on that day", listOf(otherDay), DateTimeConfidence.mismatch),
      Arguments.of("no candidates", emptyList<DateTimeCandidate>(), DateTimeConfidence.none),
    )
  }
}
