package org.migor.rich.rss.transform

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.rich.rss.service.PropertyService
import org.mockito.Mockito.mock
import java.util.*

internal class DateClaimerTest {

  private lateinit var dateClaimer: DateClaimer

  @BeforeEach
  fun setUp() {
    dateClaimer = DateClaimer(mock(PropertyService::class.java))
  }

  @Test
  fun testClaimDateFromString() {
    val dateStrings = listOf(
      Triple("2022-01-08T00:00:00", "Sat Jan 08 00:00:00 CET 2022", null),
//      Triple("06. Januar 2022, 08:00 Uhr", "", Locale.GERMAN),
      Triple("06. Januar 2022, 08:00", "Thu Jan 06 08:00:00 CET 2022", Locale.GERMAN),
//      Triple("Heute, 08:00 Uhr", Locale.GERMAN, ""),
      Triple("October 9, 2019", "Wed Oct 09 08:00:00 CEST 2019", Locale.ENGLISH),
      Triple("December 15, 2020", "Tue Dec 15 08:00:00 CET 2020", Locale.ENGLISH),
      Triple("Dezember 15, 2020", "Tue Dec 15 08:00:00 CET 2020", Locale.GERMAN),
      Triple("2022-04-28T15:50:21-07:00", "Thu Apr 28 15:50:21 CEST 2022", Locale.GERMAN),
    )
    dateStrings.forEach { (dateStr, expected, locale) ->
      run {
        val actual = dateClaimer.claimDateFromString("-", dateStr, locale)
        assertEquals(expected, actual.toString())
      }
    }
  }
}
