package org.migor.rss.rich

import com.google.gson.GsonBuilder
import org.migor.rss.rich.dto.FeedDto
import org.springframework.http.ResponseEntity
import org.thymeleaf.util.StringUtils
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.xml.stream.XMLEventFactory
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent


object FeedExporter {
  //  http://underpop.online.fr/j/java/help/modules-with-rome-xml-java.html.gz
  private val gson = GsonBuilder().create()

  // see https://validator.w3.org/feed/docs/atom.html
  fun toAtom(feed: FeedDto): ResponseEntity<String> {
    val bout = ByteArrayOutputStream()
    val (eventWriter: XMLEventWriter, eventFactory) = initXml(bout)

    eventWriter.add(eventFactory.createStartElement("", "", "feed"))
    eventWriter.add(eventFactory.createAttribute("xmlns", "http://www.w3.org/2005/Atom"))

    createNode(eventWriter, "id", "foo")
    createNode(eventWriter, "title", feed.name)
    createNode(eventWriter, "subtitle", feed.description)
    createNode(eventWriter, "updated", FeedUtil.formatAsRFC3339(feed.pubDate!!))
    createNode(eventWriter, "link", null, null, mapOf(Pair("rel", "self"), Pair("type", "application/atom+xml"), Pair("href", feed.link!!)))
//  recommended
//    createNode(eventWriter, "link", )
//    createNode(eventWriter, "author", )

//  optional
//  category, contributor, generator, icon, logo, rights

    for (entry in feed.entries!!) {
      eventWriter.add(eventFactory.createStartElement("", "", "entry"))
      createNode(eventWriter, "title", entry!!.get("title") as String?)
      createNode(eventWriter, "content", "<![CDATA[${entry.get("content")}]]")
      createNode(eventWriter, "link", null, null, mapOf(Pair("href", entry.get("link") as String)))
      createNode(eventWriter, "updated", FeedUtil.formatAsRFC3339(entry["pubDate"] as Date))
//      createNode(eventWriter, "author", entry.get("author") as String?)
      createNode(eventWriter, "id", entry.get("id") as String?)
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


  fun toRss(feed: FeedDto): ResponseEntity<String> {
    val bout = ByteArrayOutputStream()
    val (eventWriter: XMLEventWriter, eventFactory) = initXml(bout)

    eventWriter.add(eventFactory.createStartElement("", "", "rss"))
    eventWriter.add(eventFactory.createAttribute("version", "2.0"))
    eventWriter.add(eventFactory.createAttribute("xmlns:atom", "http://www.w3.org/2005/Atom"))

    eventWriter.add(eventFactory.createStartElement("", "", "channel"))

    createNode(eventWriter, "title", feed.name)
    createNode(eventWriter, "description", feed.description)
//    createNode(eventWriter, "language", feed.language)
//    createNode(eventWriter, "copyright", feed.copyright)
    createNode(eventWriter, "pubDate", FeedUtil.formatAsRFC822(feed.pubDate!!))
    createNode(eventWriter, "link", feed.link)
    createNode(eventWriter, "atom:link", null, null, mapOf(Pair("rel", "self"), Pair("type", "application/atom+xml"), Pair("href", feed.link!!)))

    for (entry in feed.entries!!) {
      eventWriter.add(eventFactory.createStartElement("", "", "item"))
      createNode(eventWriter, "title", entry!!.get("title") as String?)
      createNode(eventWriter, "description", StringUtils.escapeXml(entry.get("content")), null)
      createNode(eventWriter, "link", entry.get("link") as String?)
      createNode(eventWriter, "pubDate", FeedUtil.formatAsRFC822(entry["pubDate"] as Date))

//      createNode(eventWriter, "author", entry.get("author") as String?)
      createNode(eventWriter, "guid", entry.get("id") as String?, null, mapOf(Pair("isPermaLink", "false")))

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

  private fun createNode(eventWriter: XMLEventWriter,
                         name: String,
                         value: String?,
                         type: String? = null,
                         attributes: Map<String, String>? = null) {
    val eventFactory = XMLEventFactory.newInstance()
    val sElement = eventFactory.createStartElement("", "", name)
    type?.let {
      eventFactory.createAttribute("type", type)
    }
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

  fun toJson(feed: FeedDto): ResponseEntity<String> {
    val body = gson.toJson(feed)
    return ResponseEntity.ok()
      .header("Content-Type", "application/json; charset=utf-8")
      .body(body)
  }

}
