package org.migor.feedless.feed.exporter

import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.Description
import net.fortuna.ical4j.model.property.DtEnd
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.model.property.Summary
import net.fortuna.ical4j.model.property.Uid
import net.fortuna.ical4j.model.property.Url
import org.apache.commons.io.output.ByteArrayOutputStream
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime


@Service
class CalendarFeedExporter {

  private val log = LoggerFactory.getLogger(JsonFeedExporter::class.simpleName)

  fun toCalendar(feed: JsonFeed): String {
    val calendar = Calendar()
      .withProdId("-//Ben Fortuna//iCal4j 1.0//EN")
      .withDefaults()

    // todo https://www.ical4j.org/examples/model/
    val out = CalendarOutputter()
    return ByteArrayOutputStream().use {
      out.output(feed.items.fold(calendar) { c, item -> c.withComponent(item.toVEvent()) }.fluentTarget, it)
      it.toString(StandardCharsets.UTF_8)
    }
  }

}

private fun JsonItem.toVEvent(): CalendarComponent {
  val event = VEvent()
  event.fluentTarget
    .withProperty(Uid(id))
    .withProperty(Summary(title))
    .withProperty(Description(text))
    .withProperty(DtStart<LocalDateTime>(startingAt))
    .withProperty(DtEnd<LocalDateTime>(startingAt?.plusHours(2)))
    .withProperty(Url(URI(url)))
  return event
}
