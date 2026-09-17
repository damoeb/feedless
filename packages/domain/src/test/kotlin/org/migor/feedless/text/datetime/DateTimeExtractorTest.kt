package org.migor.feedless.text.datetime

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.util.toMillis
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

internal class DateTimeExtractorTest {

  private lateinit var dateTimeExtractor: DateTimeExtractor
  private lateinit var logCollector: LogCollector

  @BeforeEach
  fun setUp() {
    dateTimeExtractor = DateTimeExtractor()
    logCollector = LogCollector()
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "2022-01-08T00:00:00;;2022-01-08 00:00:00 CET;;de",
      "06. Januar 2022, 08:00;;2022-01-06 08:00:00 CET;;de",
      "October 9, 2019;;2019-10-09 08:00:00 CEST;;en",
      "Mai 7, 2019;;2019-05-07 08:00:00 CEST;;de",
      "März 7, 2019;;2019-03-07 08:00:00 CEST;;de",
//      "December 15, 2020;;2020-12-15 08:00:00 CET;;de", // todo test default locale en
      "December 15, 2020;;2020-12-15 08:00:00 CET;;en",
      "Dezember 15, 2020;;2020-12-15 08:00:00 CET;;de",
      "19.01.2023;;2023-01-19 08:00:00 CET;;de",
      "2022-04-28T15:50:21-07:00;;2022-04-28 15:50:21 CEST;;de",
      "2023-04-10T05:30:24.000Z;;2023-04-10 05:30:24 CEST;;de",
      "2023-04-10T05:30:24.000;;2023-04-10 05:30:24 CEST;;de",
      "Philippe Kuhn 27.09.2024 | 20:15 Uhr - 22:15 Uhr E-Mail;;2024-09-27 20:15:00 CEST;;de",
      "Marschgruppe 4. Juli 2024, 19:30 Uhr;;2024-07-04 19:30:00 CEST;;de",
      "Seniorenzmittag Mi, 21.8.24 , 12:00 bis 14:00 Uhr;;2024-08-21 08:00:00 CEST;;de",
      "12 Dezember 24;;2024-12-12 08:00:00 CET;;de",
      "12 Dez 24;;2024-12-12 08:00:00 CET;;de",
//    "8 Juli;;Thu Dec 12 08:00:00 CET 2024;;de",
    ], delimiterString = ";;"
  )
  fun testClaimDateFromString(dateStringInput: String, expectedOuput: String, lang: String) = runTest {
    val actual =
      dateTimeExtractor.extractDateTime(
        "${newCorrId()} $dateStringInput ${newCorrId()}",
        Locale.of(lang),
        logCollector
      )
    assertThat(actual).isNotNull()
    assertThat(actual!!.toMillis()).isEqualTo(
      LocalDateTime.parse(
        expectedOuput,
        DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss' 'z")
      ).toMillis()
    )
  }

  @Test
  fun `extractCandidates finds every datetime in a body`() = runTest {
    val body = """
      Konzert am 27.09.2024, 20:15 Uhr im Saal.
      Anmeldeschluss: 12.08.2024
    """.trimIndent()

    val actual = dateTimeExtractor.extractCandidates(body, Locale.GERMAN)

    assertThat(actual.map { it.dateTime }).containsExactly(
      LocalDateTime.of(2024, 9, 27, 20, 15),
      LocalDateTime.of(2024, 8, 12, 8, 0),
    )
    assertThat(actual.map { it.hasTime }).containsExactly(true, false)
    assertThat(actual[0].input).isEqualTo("27.09.2024, 20:15")
    assertThat(body.substring(actual[1].range)).isEqualTo("12.08.2024")
  }

  @Test
  fun `extractCandidates merges repeated datetimes and counts them`() = runTest {
    val body = "Beginn 4. Juli 2024, 19:30 Uhr. Wir sehen uns am 4. Juli 2024, 19:30 Uhr!"

    val actual = dateTimeExtractor.extractCandidates(body, Locale.GERMAN)

    assertThat(actual).hasSize(1)
    assertThat(actual[0].dateTime).isEqualTo(LocalDateTime.of(2024, 7, 4, 19, 30))
    assertThat(actual[0].occurrences).isEqualTo(2)
  }

  @Test
  fun `extractCandidates ignores digits inside longer numbers`() = runTest {
    val actual = dateTimeExtractor.extractCandidates("Tel. 0441 234 5678, Ticket 123456789", Locale.GERMAN)

    assertThat(actual).isEmpty()
  }

  @Test
  fun `extractCandidates returns nothing for text without dates`() = runTest {
    assertThat(dateTimeExtractor.extractCandidates("Kein Termin bekannt", Locale.GERMAN)).isEmpty()
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "n. 2025, 14.00 Uhr - 15.45 Uhr, ;;[14:00, 15:45]",
      "n. 2025, 14.00 Uhr - 15:45 Uhr, ;;[14:00, 15:45]",
      "Beginn 9:30, Ende 17:00;;[09:30, 17:00]",
      "Einlass 19.30Uhr;;[19:30]",
      "this is not a time 15.4 Uhr, ;;[]",
      "25:00 or 12:75 are no times;;[]",
      "Version 1.10.3 or 123:45;;[]",
    ],
    delimiterString = ";;"
  )
  fun `extractTimes finds isolated times`(value: String, expected: String) = runTest {
    val actual = dateTimeExtractor.extractTimes(value, Locale.GERMAN)

    assertThat(actual.map { it.time }.toString()).isEqualTo(expected)
  }

  @Test
  fun `extractTimes keeps the input string and its range`() = runTest {
    val text = "Ende gegen 22.15 Uhr"

    val actual = dateTimeExtractor.extractTimes(text, Locale.GERMAN)

    assertThat(actual).hasSize(1)
    assertThat(actual[0].input).isEqualTo("22.15")
    assertThat(text.substring(actual[0].range)).isEqualTo("22.15")
  }

  @Test
  fun `extractTimes skips times that belong to a date`() = runTest {
    val text = "Am 27.09.2024, 20:15 Uhr bis 22:15 Uhr. Nochmals: 27.09.2024, 20:15 Uhr. Stand 19.01.2023"

    val actual = dateTimeExtractor.extractTimes(text, Locale.GERMAN)

    assertThat(actual.map { it.time }).containsExactly(LocalTime.of(22, 15))
  }
}
