package org.migor.rich.rss.transform

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.rich.rss.service.PropertyService
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

internal class DateClaimerTest {
  private val log = LoggerFactory.getLogger(DateClaimerTest::class.simpleName)

  private lateinit var dateClaimer: DateClaimer

  @BeforeEach
  fun setUp() {
    dateClaimer = DateClaimer(mock(PropertyService::class.java))
  }

  @Test
  fun testClaimDateFromString() {
//    val pattern = DateTimeFormatter.ISO_DATE_TIME.parse("2023-04-10T05:30:24.000")
//    log.info(
//      pattern.format(LocalDateTime.now().atOffset(
//      ZoneOffset.UTC)))
//    log.info(pattern.withLocale(Locale.ENGLISH).parse("October 9 2019").toString())
//    log.info("parsed "+DateTimeFormatter.ISO_DATE_TIME.parse("2023-04-10T05:30:24.000").toString())
//    log.info("parsed "+DateTimeFormatter.ISO_DATE_TIME.parse("2023-04-10T05:30:24.000Z").toString())

    val dateStrings = listOf(
      Triple("2022-01-08T00:00:00", "Sat Jan 08 00:00:00 CET 2022", Locale.GERMAN),
//      Triple("06. Januar 2022, 08:00 Uhr", "", Locale.GERMAN),
      Triple("06. Januar 2022, 08:00", "Thu Jan 06 08:00:00 CET 2022", Locale.GERMAN),
//      Triple("Heute, 08:00 Uhr", Locale.GERMAN, ""),
      Triple("October 9, 2019", "Wed Oct 09 08:00:00 CEST 2019", Locale.ENGLISH),
      Triple("December 15, 2020", "Tue Dec 15 08:00:00 CET 2020", Locale.ENGLISH),
      Triple("Dezember 15, 2020", "Tue Dec 15 08:00:00 CET 2020", Locale.GERMAN),
      Triple("19.01.2023", "Thu Jan 19 08:00:00 CET 2023", Locale.GERMAN),
//      Triple("19.01.2023 - 23.01.2023", "Thu Jan 19 08:00:00 CET 2023", Locale.GERMAN),
      Triple("2022-04-28T15:50:21-07:00", "Thu Apr 28 15:50:21 CEST 2022", Locale.GERMAN),
      Triple("2023-04-10T05:30:24.000Z", "Mon Apr 10 05:30:24 CEST 2023", Locale.GERMAN),
      Triple("2023-04-10T05:30:24.000", "Mon Apr 10 05:30:24 CEST 2023", Locale.GERMAN),
    )
    dateStrings.forEach { (dateStr, expected, locale) ->
      run {
        val actual = dateClaimer.claimDatesFromString("-", dateStr, locale)
        assertEquals(expected, actual.toString())
      }
    }
  }
}
