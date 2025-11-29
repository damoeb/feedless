package org.migor.feedless.feed.parser

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.common.HttpResponse

class   CalendarFeedParserTest {
  @Test
  fun `can parse a list of ical events`() = runTest {
    val calendarFeedParser = CalendarFeedParser()

    val data =
      """
      BEGIN:VCALENDAR
      VERSION:2.0
      PRODID:-//Sample Corp//NONSGML v1.0//EN
      BEGIN:VEVENT
      UID:1@example.com
      DTSTAMP:20231117T120000Z
      DTSTART:20231120T100000Z
      DTEND:20231120T110000Z
      SUMMARY:Sample Event 1
      END:VEVENT
      BEGIN:VEVENT
      UID:2@example.com
      DTSTAMP:20231117T120000Z
      DTSTART:20231121T140000Z
      DTEND:20231121T150000Z
      SUMMARY:Sample Event 2
      END:VEVENT
      END:VCALENDAR
      """.trimIndent()
    val httpResponse = HttpResponse(
      contentType = "text/calendar; charset=UTF-8",
      url = "",
      statusCode = 200,
      responseBody = data.toByteArray(),
    )

    val feed = calendarFeedParser.process(httpResponse)
    assertThat(feed.items.size).isEqualTo(2)
  }
}
