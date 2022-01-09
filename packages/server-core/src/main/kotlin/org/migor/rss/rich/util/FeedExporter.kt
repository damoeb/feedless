package org.migor.rss.rich.util

import com.google.gson.GsonBuilder
import org.apache.commons.lang3.StringEscapeUtils
import org.migor.rss.rich.api.dto.FeedJsonDto
import org.springframework.http.ResponseEntity
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.xml.stream.XMLEventFactory
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent

object FeedExporter {
  private val GENERATOR = "rich-rss"
  const val FORMAT_RFC3339 = "yyyy-MM-dd'T'HH:mm:ss-Z"

  //  http://underpop.online.fr/j/java/help/modules-with-rome-xml-java.html.gz
  private val gson = GsonBuilder()
    .setDateFormat(FORMAT_RFC3339) // https://tools.ietf.org/html/rfc3339
    .create()

  // see https://validator.w3.org/feed/docs/atom.html
  fun toAtom(feed: FeedJsonDto): ResponseEntity<String> {
    val bout = ByteArrayOutputStream()
    val (eventWriter: XMLEventWriter, eventFactory) = initXml(bout)

    eventWriter.add(eventFactory.createStartElement("", "", "feed"))
    eventWriter.add(eventFactory.createAttribute("xmlns", "http://www.w3.org/2005/Atom"))

    val canonicalUrl = toAtomFeedUrlForPage(feed)
    createNode(eventWriter, "id", canonicalUrl)
    createNode(eventWriter, "title", feed.name)
    createNode(eventWriter, "subtitle", feed.description)
    createNode(eventWriter, "updated", FeedUtil.formatAsRFC3339(feed.date_published!!))
    createNode(
      eventWriter,
      "link",
      attributes = mapOf(Pair("rel", "self"), Pair("type", "application/atom+xml"), Pair("href", canonicalUrl))
    )
    createNode(eventWriter, "link", attributes = mapOf(Pair("href", feed.home_page_url!!)))
    createNode(eventWriter, "link", attributes = mapOf(Pair("rel", "pingback"), Pair("href", getPingbackUrl())))

    feed.selfPage?.let {
      if (feed.lastPage != feed.selfPage) {
        createNode(
          eventWriter,
          "link",
          attributes = mapOf(Pair("rel", "next"), Pair("href", toAtomFeedUrlForPage(feed, feed.selfPage + 1)))
        )
      }
      if (feed.selfPage != 0) {
        createNode(
          eventWriter,
          "link",
          attributes = mapOf(Pair("rel", "previous"), Pair("href", toAtomFeedUrlForPage(feed, feed.selfPage - 1)))
        )
      }
    }
    createNode(
      eventWriter,
      "link",
      attributes = mapOf(Pair("rel", "last"), Pair("href", toAtomFeedUrlForPage(feed, feed.lastPage)))
    )

    createNode(eventWriter, "generator", GENERATOR)

//  optional
//  category, contributor, icon, logo, rights

    for (entry in feed.items!!) {
      eventWriter.add(eventFactory.createStartElement("", "", "entry"))
      createNode(eventWriter, "title", entry!!.title)
      createNode(eventWriter, "summary", entry.content_text)
      entry.content_raw?.let {
        createNode(
          eventWriter,
          "content",
          "<![CDATA[${entry.content_raw}]]",
          mapOf(Pair("type", entry.content_raw_mime!!))
        )
      }
      createNode(eventWriter, "link", attributes = mapOf(Pair("href", entry.url)))
      entry.main_image_url?.let {
        // todo mag wire up article
        // there are more links like  "alternate", "related", "self", "enclosure", and "via" https://web.archive.org/web/20071009193151/http://atompub.org/2005/03/12/draft-ietf-atompub-format-06.html
        createNode(
          eventWriter,
          "link",
          attributes = mapOf(Pair("rel", "enclosure"), Pair("type", "image"), Pair("href", it))
        )
      }

      createNode(eventWriter, "updated", FeedUtil.formatAsRFC3339(entry.date_published))

      eventWriter.add(eventFactory.createStartElement("", "", "author"))
      createNode(eventWriter, "name", entry.author)
      eventWriter.add(eventFactory.createEndElement("", "", "author"))

      createNode(eventWriter, "id", entry.id)
      eventWriter.add(eventFactory.createEndElement("", "", "entry"))

//    category, published
//    rights
//      <rights type="html">
//      &amp;copy; 2005 John Doe
//      </rights>

//    source
//      <source>
//      <id>http://example.org/</id>
//      <title>Example, Inc.</title>
//      <updated>2003-12-13T18:30:02Z</updated>
//      </source>
    }

    eventWriter.add(eventFactory.createEndElement("", "", "feed"))

    eventWriter.add(eventFactory.createEndDocument())

    eventWriter.close()

    val body = bout.toString(StandardCharsets.UTF_8)
    return ResponseEntity.ok()
      .header("Content-Type", "application/atom+xml; charset=utf-8")
      .body(body)
  }

  private fun getPingbackUrl(): String {
    return "https://localhost:8080/pingback.ping"
  }

  private fun toAtomFeedUrlForPage(feed: FeedJsonDto, page: Int? = null): String {
    return toFeedUrlForPage(feed, "atom", page)
  }

  private fun toJsonFeedUrlForPage(feed: FeedJsonDto, page: Int? = null): String {
    return toFeedUrlForPage(feed, "json", page)
  }

  private fun toFeedUrlForPage(feed: FeedJsonDto, type: String, page: Int? = null): String {
    return Optional.ofNullable(page).map { actualPage -> "${feed.feed_url}/${type}?page=${actualPage}" }.orElse(feed.feed_url)
  }

  fun toRss(feed: FeedJsonDto): ResponseEntity<String> {
    val bout = ByteArrayOutputStream()
    val (eventWriter: XMLEventWriter, eventFactory) = initXml(bout)

    eventWriter.add(eventFactory.createStartElement("", "", "rss"))
    eventWriter.add(eventFactory.createAttribute("version", "2.0"))
    eventWriter.add(eventFactory.createAttribute("xmlns:atom", "http://www.w3.org/2005/Atom"))

    eventWriter.add(eventFactory.createStartElement("", "", "channel"))

    createNode(eventWriter, "title", feed.name)
    createNode(eventWriter, "description", feed.description)
    createNode(eventWriter, "generator", GENERATOR)
//    createNode(eventWriter, "language", feed.language)
//    createNode(eventWriter, "copyright", feed.copyright)
    createNode(eventWriter, "pubDate", FeedUtil.formatAsRFC822(feed.date_published!!))

    val canonicalUrl = toAtomFeedUrlForPage(feed, feed.selfPage)
    createNode(eventWriter, "link", canonicalUrl, mapOf(Pair("type", "application/atom+xml")))

    for (item in feed.items!!) {
      eventWriter.add(eventFactory.createStartElement("", "", "item"))
      createNode(eventWriter, "title", item!!.title)
      createNode(eventWriter, "description", StringEscapeUtils.escapeXml(item.content_text))
      createNode(eventWriter, "link", item.url)
      createNode(eventWriter, "pubDate", FeedUtil.formatAsRFC822(item.date_published))

//      createNode(eventWriter, "author", entry.get("author") as String?)
      createNode(eventWriter, "guid", item.id, mapOf(Pair("isPermaLink", "false")))

      eventWriter.add(eventFactory.createEndElement("", "", "item"))
    }

    eventWriter.add(eventFactory.createEndElement("", "", "channel"))
    eventWriter.add(eventFactory.createEndElement("", "", "rss"))

    eventWriter.add(eventFactory.createEndDocument())

    eventWriter.close()

    val body = bout.toString(StandardCharsets.UTF_8)
    return ResponseEntity.ok()
      .header("Content-Type", "application/rss+xml; charset=utf-8")
      .body(body)
  }

  private fun initXml(bout: ByteArrayOutputStream): Triple<XMLEventWriter, XMLEventFactory, XMLEvent> {
    val outputFactory = XMLOutputFactory.newInstance()

    val eventWriter: XMLEventWriter = outputFactory
      .createXMLEventWriter(bout, "UTF-8")

    val eventFactory = XMLEventFactory.newInstance()
    val end: XMLEvent = eventFactory.createDTD("\n")

    val startDocument = eventFactory.createStartDocument("UTF-8", "1.0")
    eventWriter.add(startDocument)
    eventWriter.add(end)
    return Triple(eventWriter, eventFactory, end)
  }

  private fun createNode(
    eventWriter: XMLEventWriter,
    name: String,
    value: String? = null,
    attributes: Map<String, String>? = null
  ) {
    val eventFactory = XMLEventFactory.newInstance()
    val sElement = eventFactory.createStartElement("", "", name)
    eventWriter.add(sElement)

    attributes?.let {
      attributes.forEach { (key, value) -> eventWriter.add(eventFactory.createAttribute(key, value)) }
    }

    if (value != null) {
      val characters: Characters = eventFactory.createCharacters(value)
      eventWriter.add(characters)
    }

    val eElement = eventFactory.createEndElement("", "", name)
    eventWriter.add(eElement)
  }

  fun toJson(feed: FeedJsonDto): ResponseEntity<String> {
    feed.selfPage?.let {
      if (feed.lastPage != feed.selfPage) {
        feed.next_url = toJsonFeedUrlForPage(feed, feed.selfPage + 1)
      }
      if (feed.selfPage != 0) {
        feed.previous_url = toJsonFeedUrlForPage(feed, feed.selfPage - 1)
      }
    }
    feed.last_url = toJsonFeedUrlForPage(feed, feed.lastPage)

    val body = gson.toJson(feed)
    return ResponseEntity.ok()
      .header("Content-Type", "application/json; charset=utf-8")
      .body(body)
  }
}
