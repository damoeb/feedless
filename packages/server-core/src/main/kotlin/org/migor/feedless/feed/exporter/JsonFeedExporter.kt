package org.migor.feedless.feed.exporter

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.migor.feedless.feed.parser.json.JsonFeed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter



@Service
@Transactional(propagation = Propagation.NEVER)
class JsonFeedExporter {

  private val log = LoggerFactory.getLogger(JsonFeedExporter::class.simpleName)

  //  http://underpop.online.fr/j/java/help/modules-with-rome-xml-java.html.gz
  private val gson = GsonBuilder()
    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
    .create()

  fun toJson(feed: JsonFeed): String {
//    log.info("to json")

//    feed.selfPage?.let {
//      if (feed.items.isNotEmpty()) {
//        feed.nextUrl = toJsonFeedUrlForPage(feed, it + 1)
//      }
////      if (feed.selfPage != 0) {
////        feed.previousUrl = toJsonFeedUrlForPage(feed, it - 1)
////      }
//    }
//    feed.lastUrl = toJsonFeedUrlForPage(feed, feed.lastPage)
    return gson.toJson(feed)
  }

}


class LocalDateTimeTypeAdapter : JsonSerializer<LocalDateTime?>, JsonDeserializer<LocalDateTime?> {
  private val FORMAT_RFC3339 = "yyyy-MM-dd'T'HH:mm:ss-Z"
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_RFC3339)

  override fun serialize(date: LocalDateTime?, typeOf: Type?, context: JsonSerializationContext): JsonElement {
    return JsonPrimitive(date?.let { formatter.format(date.atOffset(ZoneOffset.UTC)) })
  }

  override fun deserialize(json: JsonElement?, typeOf: Type?, context: JsonDeserializationContext): LocalDateTime? {
    return json?.let { LocalDateTime.parse(it.asString, formatter)}
  }
}
