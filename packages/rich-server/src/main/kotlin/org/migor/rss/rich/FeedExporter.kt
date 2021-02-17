package org.migor.rss.rich

import com.google.gson.GsonBuilder
import org.migor.rss.rich.dto.FeedDto
import org.springframework.http.ResponseEntity
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import javax.xml.stream.XMLEventFactory
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent


object FeedExporter {
  //  http://underpop.online.fr/j/java/help/modules-with-rome-xml-java.html.gz
  private val gson = GsonBuilder().create()

  fun toAtom(feed: FeedDto): ResponseEntity<String> {
    val bout = ByteArrayOutputStream()
    val (eventWriter: XMLEventWriter, eventFactory, end: XMLEvent) = initXml(bout)

    eventWriter.add(eventFactory.createStartElement("", "", "feed"))
    eventWriter.add(eventFactory.createAttribute("xmlns", "http://www.w3.org/2005/Atom"))
    eventWriter.add(end)

    createNode(eventWriter, "title", feed.name)
//    createNode(eventWriter, "link", feed.link)
    createNode(eventWriter, "description", feed.description)
//    createNode(eventWriter, "language", feed.language)
//    createNode(eventWriter, "copyright", feed.copyright)
    createNode(eventWriter, "updated", feed.pubDate.toString())

    for (entry in feed.entries!!) {
      eventWriter.add(eventFactory.createStartElement("", "", "entry"))
      eventWriter.add(end)
      createNode(eventWriter, "title", entry!!.get("title") as String?)
      val description: Map<String, String> = entry.get("description") as Map<String, String>
      createNode(eventWriter, "description", "<![CDATA[${description.get("value")}]]", description.get("type"))
      createNode(eventWriter, "link", entry.get("link") as String?)
      createNode(eventWriter, "author", entry.get("author") as String?)
      createNode(eventWriter, "guid", entry.get("id") as String?)
      eventWriter.add(end)
      eventWriter.add(eventFactory.createEndElement("", "", "entry"))
      eventWriter.add(end)
    }

    eventWriter.add(end)
    eventWriter.add(eventFactory.createEndElement("", "", "feed"))

    eventWriter.add(end)

    eventWriter.add(eventFactory.createEndDocument())

    eventWriter.close()

    val body = bout.toString(StandardCharsets.UTF_8)
    return ResponseEntity.ok()
      .header("Content-Type", "application/atom+xml; charset=utf-8")
      .body(body)
  }

  fun toRss(feed: FeedDto): ResponseEntity<String> {
    val bout = ByteArrayOutputStream()
    val (eventWriter: XMLEventWriter, eventFactory, end: XMLEvent) = initXml(bout)

    eventWriter.add(eventFactory.createStartElement("", "", "rss"))
    eventWriter.add(eventFactory.createAttribute("version", "2.0"))
    eventWriter.add(end)

    eventWriter.add(eventFactory.createStartElement("", "", "channel"))
    eventWriter.add(end)

    createNode(eventWriter, "title", feed.name)
    createNode(eventWriter, "description", feed.description)
//    createNode(eventWriter, "language", feed.language)
//    createNode(eventWriter, "copyright", feed.copyright)
    createNode(eventWriter, "pubdate", feed.pubDate.toString())
//    createNode(eventWriter, "link", feed.link)

    for (entry in feed.entries!!) {
      eventWriter.add(eventFactory.createStartElement("", "", "item"))
      eventWriter.add(end)
      createNode(eventWriter, "title", entry!!.get("title") as String?)
      val description: Map<String, String> = entry.get("description") as Map<String, String>
      createNode(eventWriter, "description", "<![CDATA[${description.get("value")}]]", description.get("type"))
      createNode(eventWriter, "link", entry.get("link") as String?)
      createNode(eventWriter, "author", entry.get("author") as String?)
      createNode(eventWriter, "guid", entry.get("id") as String?)
      eventWriter.add(end)
      eventWriter.add(eventFactory.createEndElement("", "", "item"))
      eventWriter.add(end)
    }

    eventWriter.add(end)
    eventWriter.add(eventFactory.createEndElement("", "", "channel"))
    eventWriter.add(end)
    eventWriter.add(eventFactory.createEndElement("", "", "rss"))

    eventWriter.add(end)

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
                         type: String? = null) {
    val eventFactory = XMLEventFactory.newInstance()
    val end: XMLEvent = eventFactory.createDTD("\n")
    val tab: XMLEvent = eventFactory.createDTD("\t")
    val sElement = eventFactory.createStartElement("", "", name)
    type?.let {
      eventFactory.createAttribute("type", type)
    }
    eventWriter.add(tab)
    eventWriter.add(sElement)
    val characters: Characters = eventFactory.createCharacters(value)
    eventWriter.add(characters)
    val eElement = eventFactory.createEndElement("", "", name)
    eventWriter.add(eElement)
    eventWriter.add(end)
  }

  fun toJson(feed: FeedDto): ResponseEntity<String> {
    val body = gson.toJson(feed)
    return ResponseEntity.ok()
      .header("Content-Type", "application/json; charset=utf-8")
      .body(body)
  }

}
