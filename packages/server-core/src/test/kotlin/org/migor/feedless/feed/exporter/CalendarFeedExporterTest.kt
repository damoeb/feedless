package org.migor.feedless.feed.exporter

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.feed.parser.CalendarFeedParser
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import java.time.LocalDateTime
import java.util.*

class CalendarFeedExporterTest {

  private lateinit var exporter: CalendarFeedExporter
  private lateinit var parser: CalendarFeedParser

  @BeforeEach
  fun setUp() {
    exporter = CalendarFeedExporter()
    parser = CalendarFeedParser()
  }

  @Test
  fun toCalendar() = runTest {
    val feed = JsonFeed()
    feed.id = UUID.randomUUID().toString()
    feed.title = "foo"
    feed.items = listOf(createJsonItem())
    val ical = exporter.toCalendar(feed)
    val feedAfter = parser.parse(ical, "")
    assertThat(feedAfter).isNotNull
    assertThat(feedAfter.items).isNotEmpty
  }

  private fun createJsonItem(): JsonItem {
    val item = JsonItem()
    item.id = UUID.randomUUID().toString()
    item.title = " foo is the perfect title"
    item.text = "bar is the payload"
    item.url = "https://foo.bar/1"
    item.startingAt = LocalDateTime.now()
    return item
  }

}
