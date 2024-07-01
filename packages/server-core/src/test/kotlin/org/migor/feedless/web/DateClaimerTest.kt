package org.migor.feedless.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.common.PropertyService
import org.migor.feedless.feed.DateClaimer
import org.migor.feedless.util.CryptUtil.newCorrId
import org.mockito.Mockito.mock
import org.slf4j.LoggerFactory
import java.util.*

internal class DateClaimerTest {
  private val log = LoggerFactory.getLogger(DateClaimerTest::class.simpleName)

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

//      11:01:34.730 ERROR DateClaimer - [M2ZJV] Cannot parse dateString «L¨CKE» Patti Basler & Philippe Kuhn 27.09.2024 | 20:15 Uhr - 22:15 Uhr E-Mail, Homepage
//    11:01:34.733 ERROR DateClaimer - [M2ZJV] Cannot parse dateString Mittagstisch für Seniorinnen und Senioren 01.10.2024 | 11:15 Uhr - 14:00 Uhr E-Mail, Homepage
//    11:01:34.739 ERROR DateClaimer - [M2ZJV] Cannot parse dateString Gemeindeversammlung (Eventuell / Ausserordentlich) 03.10.2024 | 19:00 Uhr - 22:00 Uhr E-Mail, Homepage
//    11:01:34.744 ERROR DateClaimer - [M2ZJV] Cannot parse dateString Mittagstisch für Seniorinnen und Senioren 15.10.2024 | 11:15 Uhr - 14:00 Uhr E-Mail, Homepage
  ], delimiterString = ";;")
  fun testClaimDateFromString(dateStringInput: String, expectedOuput: String, lang: String) {
//    val pattern = DateTimeFormatter.ISO_DATE_TIME.parse("2023-04-10T05:30:24.000")
//    log.info(
//      pattern.format(LocalDateTime.now().atOffset(
//      ZoneOffset.UTC)))
//    log.info(pattern.withLocale(Locale.ENGLISH).parse("October 9 2019").toString())
//    log.info("parsed "+DateTimeFormatter.ISO_DATE_TIME.parse("2023-04-10T05:30:24.000").toString())
//    log.info("parsed "+DateTimeFormatter.ISO_DATE_TIME.parse("2023-04-10T05:30:24.000Z").toString())

//      Triple("06. Januar 2022, 08:00 Uhr", "", Locale.GERMAN),
//      Triple("Heute, 08:00 Uhr", Locale.GERMAN, ""),
//      Triple("19.01.2023 - 23.01.2023", "Thu Jan 19 08:00:00 CET 2023", Locale.GERMAN),
    val corrId = "test"

    val actual = dateClaimer.claimDatesFromString(corrId, "${newCorrId()} $dateStringInput ${newCorrId()}", Locale.of(lang))
    assertThat(actual.toString()).isEqualTo(expectedOuput)
  }
}
