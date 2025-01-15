package org.migor.feedless.feed

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.scrape.LogCollector

class TimeClaimerTest {

  private lateinit var timeClaimer: TimeClaimer
  private lateinit var logCollector: LogCollector

  @BeforeEach
  fun setUp() {
    timeClaimer = TimeClaimer()
    logCollector = LogCollector()
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "n. 2025, 14.00 Uhr - 15.45 Uhr, ;;[14:00, 15:45]",
      "n. 2025, 14.00 Uhr - 15:45 Uhr, ;;[14:00, 15:45]",
      "this is not a time 15.4 Uhr, ;;[]",
    ],
    delimiterString = ";;"
  )
  fun testParser(value: String, expected: String) = runTest{
    val times = timeClaimer.claimTimeFromString(value, logCollector)
    assertThat(times.map { it.second }.toString()).isEqualTo(expected)
  }
}
