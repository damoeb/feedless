package org.migor.rich.rss.exporter

import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.xml.stream.XMLEventFactory
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent
import kotlin.time.Duration

@Service
class AtomFeedExporter {
  private val GENERATOR = "rich-rss"
//  private val modules = listOf(
//    Pair("atom", AtomLinkModule.URI),
//    Pair("itunes", FeedInformation.URI),
//    Pair("media", MediaModule.URI),
//    Pair("os", OpenSearchModule.URI),
//    Pair("photocast", PhotocastModule.URI),
//    Pair("cc", CreativeCommons.URI),
//    Pair("content", ContentModule.URI),
//    Pair("slash", Slash.URI),
//  )

  private val log = LoggerFactory.getLogger(AtomFeedExporter::class.simpleName)

  // see https://validator.w3.org/feed/docs/atom.html
// see https://validator.w3.org/feed/docs/atom.html
  fun toAtom(corrId: String, feed: RichFeed, maxAge: Duration? = null): String {
    log.info("[${corrId}] to atom")
    val bout = ByteArrayOutputStream()
    val (eventWriter: XMLEventWriter, eventFactory) = initXml(bout)

    eventWriter.add(eventFactory.createStartElement("", "", "feed"))
    eventWriter.add(eventFactory.createAttribute("xmlns", "http://www.w3.org/2005/Atom"))

    val canonicalUrl = toAtomFeedUrlForPage(feed)
    createNode(eventWriter, "id", canonicalUrl)
    createNode(eventWriter, "title", feed.title)
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

    for (entry in feed.items) {
      eventWriter.add(eventFactory.createStartElement("", "", "entry"))
      createNode(eventWriter, "title", entry.title)
      createNode(eventWriter, "summary", entry.contentText)
      entry.contentRaw?.let {
        createNode(
          eventWriter,
          "content",
          "<![CDATA[${entry.contentRaw}]]",
          mapOf(Pair("type", entry.contentRawMime!!))
        )
      }
      createNode(eventWriter, "link", attributes = mapOf(Pair("href", entry.url)))
      entry.imageUrl?.let {
        // todo mag wire up article
        // there are more links like  "alternate", "related", "self", "enclosure", and "via" https://web.archive.org/web/20071009193151/http://atompub.org/2005/03/12/draft-ietf-atompub-format-06.html
        createNode(
          eventWriter,
          "link",
          attributes = mapOf(Pair("rel", "enclosure"), Pair("type", "image"), Pair("href", it))
        )
      }

      createNode(eventWriter, "updated", FeedUtil.formatAsRFC3339(entry.publishedAt))

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

    return bout.toString(StandardCharsets.UTF_8)
  }

//  private fun writeModule(prefix: String, module: Module?, eventWriter: XMLEventWriter) {
//    val createPrefixedNode: (String, Any?) -> Unit = { name, value -> value?.let { createNode(eventWriter, "${prefix}:${name}", "${it}") }}
//    module?.let {
//      if (module is AtomLinkModule) {
//        module.links?.let {
//          for (link in module.links) {
//            createNode(eventWriter, "link", attributes = mapOf(Pair("rel", link.rel), Pair("href", link.href)))
//          }
//        }
//      }
//      if (module is FeedInformation) {
//        createPrefixedNode("type", module.type)
//        createPrefixedNode("author", module.author)
//        createPrefixedNode("complete", module.complete)
//        createPrefixedNode("newFeedUrl", module.newFeedUrl)
//        createPrefixedNode("ownerName", module.ownerName)
//        createPrefixedNode("ownerEmailAddress", module.ownerEmailAddress)
//        createPrefixedNode("explicit", module.explicit)
//        createPrefixedNode("subtitle", module.subtitle)
//        module.categories.forEach {
//          createPrefixedNode("category", it.name)
//        }
//      }
//      if (module is EntryInformation) {
//        createPrefixedNode("explicit", module.explicit)
//        createPrefixedNode("title", module.title)
//        createPrefixedNode("closedCaptioned", module.closedCaptioned)
//        createPrefixedNode("duration", module.duration)
//        createPrefixedNode("episode", module.episode)
//        createPrefixedNode("episodeType", module.episodeType)
//        createPrefixedNode("order", module.order)
//        createPrefixedNode("season", module.season)
//        createPrefixedNode("author", module.author)
//        createPrefixedNode("type", module.imageUri)
//      }
////      if (module is MediaModule) {
////        createPrefixedNode("player", module.player)
////      }
////      if (module is ContentModule) {
////        module.contentItems.forEach {
////          createPrefixedNode("section", it.)
////        }
////      }
//      if (module is Slash) {
//        createPrefixedNode("comments", module.comments)
//        createPrefixedNode("department", module.department)
//        createPrefixedNode("section", module.section)
//      }
//    }
//  }

  private fun getPingbackUrl(): String {
    return "https://localhost:8080/pingback.ping"
  }

  private fun toAtomFeedUrlForPage(feed: RichFeed, page: Int? = null): String {
    return toFeedUrlForPage(feed, page)
  }

  private fun toFeedUrlForPage(feed: RichFeed, page: Int? = null): String {
    return Optional.ofNullable(page).map { actualPage -> "${feed.feed_url}/atom?page=${actualPage}" }
      .orElse(feed.feed_url)
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
}
