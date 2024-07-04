package org.migor.feedless.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.common.PropertyService
import org.migor.feedless.feed.DateClaimer
import org.migor.feedless.util.CryptUtil.newCorrId
import org.mockito.Mockito.mock
import java.util.*

internal class DateClaimerTest {
  private val corrId = "test"

  private lateinit var dateClaimer: DateClaimer

  @BeforeEach
  fun setUp() {
    dateClaimer = DateClaimer(mock(PropertyService::class.java))
  }

  @ParameterizedTest
  @CsvSource(value = [
    "2022-01-08T00:00:00;;Sat Jan 08 00:00:00 CET 2022;;de",
    "06. Januar 2022, 08:00;;Thu Jan 06 08:00:00 CET 2022;;de",
    "October 9, 2019;;Wed Oct 09 08:00:00 CEST 2019;;en",
    "December 15, 2020;;Tue Dec 15 08:00:00 CET 2020;;en",
    "Dezember 15, 2020;;Tue Dec 15 08:00:00 CET 2020;;de",
    "19.01.2023;;Thu Jan 19 08:00:00 CET 2023;;de",
    "2022-04-28T15:50:21-07:00;;Thu Apr 28 15:50:21 CEST 2022;;de",
    "2023-04-10T05:30:24.000Z;;Mon Apr 10 05:30:24 CEST 2023;;de",
    "2023-04-10T05:30:24.000;;Mon Apr 10 05:30:24 CEST 2023;;de",
    "Philippe Kuhn 27.09.2024 | 20:15 Uhr - 22:15 Uhr E-Mail;;Fri Sep 27 20:15:00 CEST 2024;;de",
    "Marschgruppe 4. Juli 2024, 19:30 Uhr;;Thu Jul 04 19:30:00 CEST 2024;;de",
    "Seniorenzmittag Mi, 21.8.24 , 12:00 bis 14:00 Uhr;;Wed Aug 21 08:00:00 CEST 2024;;de",
    "12 Dezember 24;;Thu Dec 12 08:00:00 CET 2024;;de",
    "12 Dez 24;;Thu Dec 12 08:00:00 CET 2024;;de",
//    "8 Juli;;Thu Dec 12 08:00:00 CET 2024;;de",
  ], delimiterString = ";;")
  fun testClaimDateFromString(dateStringInput: String, expectedOuput: String, lang: String) {
    val actual = dateClaimer.claimDatesFromString(corrId, "${newCorrId()} $dateStringInput ${newCorrId()}", Locale.of(lang))
    assertThat(actual.toString()).isEqualTo(expectedOuput)
  }
}
