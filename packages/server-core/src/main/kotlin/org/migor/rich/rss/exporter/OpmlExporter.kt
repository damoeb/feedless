package org.migor.rich.rss.exporter

import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.xml.stream.XMLEventWriter

@Service
class OpmlExporter : AbstractXmlExporter() {

  private val log = LoggerFactory.getLogger(OpmlExporter::class.simpleName)

  fun toOpml(corrId: String, feeds: List<NativeFeedEntity>): String {
    log.info("[${corrId}] to opml")

    val bout = ByteArrayOutputStream()
    val (eventWriter: XMLEventWriter, eventFactory) = initXml(bout)

    eventWriter.add(eventFactory.createStartElement("", "", "opml"))
    eventWriter.add(eventFactory.createAttribute("version", "2.0"))

    eventWriter.add(eventFactory.createStartElement("", "", "head"))
    createNode(eventWriter, "title", "rich-rss")
    createNode(eventWriter, "dateCreated", formatAsRFC822(Date()))
//    <dateModified>Tue, 02 Aug 2005 21:42:48 GMT</dateModified>
//    <ownerName>Dave Winer</ownerName>
//    <ownerEmail>dave@scripting.com</ownerEmail>
//    <expansionState></expansionState>
//    <vertScrollState>1</vertScrollState>
//    <windowTop>61</windowTop>
//    <windowLeft>304</windowLeft>
//    <windowBottom>562</windowBottom>
//    <windowRight>842</windowRight>

    eventWriter.add(eventFactory.createEndElement("", "", "head"))


    eventWriter.add(eventFactory.createStartElement("", "", "body"))

    for (entry in feeds) {
      eventWriter.add(eventFactory.createStartElement("", "", "outline"))
      createNode(eventWriter, "title", entry.title)

//      text="CNET News.com"
//      description="Tech news and business reports by CNET News.com. Focused on information technology, core topics include computers, hardware, software, networking, and Internet media."
//      htmlUrl="http://news.com.com/"
//      title="CNET News.com"
//      type="rss"
//      version="RSS2"
//      xmlUrl="http://news.com.com/2547-1_3-0-5.xml"
      eventWriter.add(eventFactory.createEndElement("", "", "outline"))
    }
    eventWriter.add(eventFactory.createEndElement("", "", "body"))

    eventWriter.add(eventFactory.createEndElement("", "", "opml"))

    eventWriter.add(eventFactory.createEndDocument())

    eventWriter.close()

    return bout.toString(StandardCharsets.UTF_8)
  }
}
