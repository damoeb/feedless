package org.migor.feedless.web

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.feed.DateClaimer
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.util.toDate
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

internal class DateClaimerTest {
  private val corrId = "test"

  private lateinit var dateClaimer: DateClaimer

  @BeforeEach
  fun setUp() {
    dateClaimer = DateClaimer()
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "2022-01-08T00:00:00;;2022-01-08 00:00:00 CET;;de",
      "06. Januar 2022, 08:00;;2022-01-06 08:00:00 CET;;de",
      "October 9, 2019;;2019-10-09 08:00:00 CEST;;en",
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
      dateClaimer.claimDatesFromString(corrId, "${newCorrId()} $dateStringInput ${newCorrId()}", Locale.of(lang))
    assertThat(actual!!.time).isEqualTo(
      toDate(
        SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss' 'Z").parse(expectedOuput).toInstant().atZone(
          ZoneId.systemDefault()
        )
      ).time
    )
  }
}
