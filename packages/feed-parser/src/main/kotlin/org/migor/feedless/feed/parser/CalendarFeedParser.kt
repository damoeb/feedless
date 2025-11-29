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
import java.time.ZoneOffset
import java.time.temporal.Temporal
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
    return parse(String(response.responseBody), response.url)
  }

  suspend fun parse(response: String, url: String): JsonFeed {
    val sin = StringReader(response)
    val builder = CalendarBuilder()
    val calendar = builder.build(sin)

    val feed = JsonFeed()
    feed.id = "calendar.uid.value"
    feed.title = "vcalendar"
    feed.publishedAt = LocalDateTime.now()
    feed.feedUrl = url
    feed.items = calendar.getComponents<CalendarComponent>()
      .filterIsInstance<VEvent>()
      .map { it.toJsonItem() }
    return feed
  }
}

private fun VEvent.toJsonItem(): JsonItem {
  val item = JsonItem()
  item.id = uid.getOrNull()?.value ?: ""
  item.title = summary?.value ?: ""
  item.url = url?.value ?: ""
  item.text = description?.value ?: ""
  item.tags = categories?.map { it.value }?.toList()
  item.startingAt = LocalDateTime.from(getDateTimeStart<Temporal>().date)
  item.endingAt = LocalDateTime.from(getDateTimeEnd<Temporal>().date)
  val lastModified = lastModified?.date?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }
  item.publishedAt = lastModified ?: LocalDateTime.now()
  item.modifiedAt = lastModified
  return item
}
