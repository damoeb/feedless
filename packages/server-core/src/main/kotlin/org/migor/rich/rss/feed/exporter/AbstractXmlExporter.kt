package org.migor.rich.rss.feed.exporter

import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.stream.XMLEventFactory
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent

abstract class AbstractXmlExporter {

  // see https://stackoverflow.com/questions/15247742/rfc-822-date-time-format-in-rss-2-0-feeds-cet-not-accepted
  private val rfc822DateFormatter: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
  private val rfc3339DateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")

  protected fun formatAsRFC822(date: Date): String {
    return rfc822DateFormatter.format(date)
  }

  protected fun formatAsRFC3339(date: Date): String {
    return rfc3339DateFormatter.format(date)
  }

  protected fun initXml(bout: ByteArrayOutputStream): Triple<XMLEventWriter, XMLEventFactory, XMLEvent> {
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

  protected fun createNode(
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
}
