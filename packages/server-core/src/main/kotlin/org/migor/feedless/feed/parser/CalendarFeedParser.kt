package org.migor.feedless.feed.parser

import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.model.component.VEvent
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.slf4j.LoggerFactory
import java.io.StringReader
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.jvm.optionals.getOrNull


class CalendarFeedParser : FeedBodyParser {

  private val log = LoggerFactory.getLogger(CalendarFeedParser::class.simpleName)

  override fun priority(): Int {
    return 1
  }

  override fun canProcess(feedType: FeedType): Boolean {
    return feedType == FeedType.CALENDAR // text/calendar
  }

  override suspend fun process(response: HttpResponse): JsonFeed {
    val sin = StringReader(String(response.responseBody))
    val builder = CalendarBuilder()
    val calendar = builder.build(sin)

    val feed = JsonFeed()
    feed.id = "calendar.uid.value"
    feed.title = "vcalendar"
    feed.publishedAt = LocalDateTime.now()
    feed.feedUrl = response.url
    feed.items = calendar.getComponents<CalendarComponent>()
      .filterIsInstance<VEvent>()
      .map { it.toJsonItem() }
    return feed
  }
}

private fun VEvent.toJsonItem(): JsonItem {
  val item = JsonItem()
  item.id = uid.get().value
  item.title = summary.get().value
  item.url = url.getOrNull()?.value ?: ""
  item.text = description.getOrNull()?.value ?: ""
  item.tags = categories.getOrNull()?.categories?.texts?.toList()
  item.startingAt = getDateTimeStart<OffsetDateTime>().get().date.toLocalDateTime()
  item.endingAt = getDateTimeEnd<OffsetDateTime>().get().date.toLocalDateTime()
  val lastModified = lastModified?.getOrNull()?.date?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }
  item.publishedAt = lastModified ?: LocalDateTime.now()
  item.modifiedAt = lastModified
  return item
}
