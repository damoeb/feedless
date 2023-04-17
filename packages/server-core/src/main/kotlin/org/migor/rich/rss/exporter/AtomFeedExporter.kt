package org.migor.rich.rss.exporter

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.RichFeed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.xml.stream.XMLEventWriter
import kotlin.time.Duration

@Service
class AtomFeedExporter : AbstractXmlExporter() {
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
  fun toAtom(corrId: String, feed: RichFeed, maxAge: Duration? = null): String {
    log.info("[${corrId}] to atom")
//    val syndFeed = FeedUtil.toSyndFeed(feed)
//    val output = SyndFeedOutput()
//    return output.outputString(syndFeed, true)
    val bout = ByteArrayOutputStream()
    val (eventWriter: XMLEventWriter, eventFactory) = initXml(bout)

    eventWriter.add(eventFactory.createStartElement("", "", "feed"))
    eventWriter.add(eventFactory.createAttribute("xmlns", "http://www.w3.org/2005/Atom"))

    val canonicalUrl = toAtomFeedUrlForPage(feed)
    createNode(eventWriter, "id", canonicalUrl)
    createNode(eventWriter, "title", feed.title)
    if (StringUtils.isNoneBlank(feed.imageUrl)) {
      createNode(eventWriter, "logo", feed.imageUrl)
    }
    if (StringUtils.isNoneBlank(feed.iconUrl)) {
      createNode(eventWriter, "icon", feed.iconUrl)
    }
    createNode(eventWriter, "subtitle", feed.description)
    createNode(eventWriter, "updated", formatAsRFC3339(feed.publishedAt))
    createNode(
      eventWriter,
      "link",
      attributes = mapOf(Pair("rel", "self"), Pair("type", "application/atom+xml"), Pair("href", canonicalUrl))
    )
    createNode(eventWriter, "link", attributes = mapOf(Pair("href", feed.websiteUrl!!)))
    createNode(eventWriter, "link", attributes = mapOf(Pair("rel", "pingback"), Pair("href", getPingbackUrl())))

    feed.selfPage?.let {
      if (feed.selfPage != 0) {
        createNode(
          eventWriter,
          "link",
          attributes = mapOf(Pair("rel", "previous"), Pair("href", toAtomFeedUrlForPage(feed, it - 1)))
        )
      }
    }
//    createNode(
//      eventWriter,
//      "link",
//      attributes = mapOf(Pair("rel", "last"), Pair("href", toAtomFeedUrlForPage(feed, feed.lastPage)))
//    )

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

      createNode(eventWriter, "published", formatAsRFC3339(entry.publishedAt))
      entry.startingAt?.let {
        createNode(eventWriter, "startingAt", formatAsRFC3339(it))
      }
//      createNode(eventWriter, "updated", formatAsRFC3339(entry.publishedAt))

//      if (StringUtils.isNoneBlank(entry.author)) {
//        eventWriter.add(eventFactory.createStartElement("", "", "author"))
//        createNode(eventWriter, "name", entry.author)
//        eventWriter.add(eventFactory.createEndElement("", "", "author"))
//      }

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

  private fun getPingbackUrl(): String {
    return "https://localhost:8080/pingback.ping"
  }

  private fun toAtomFeedUrlForPage(feed: RichFeed, page: Int? = null): String {
    return toFeedUrlForPage(feed, page)
  }

  private fun toFeedUrlForPage(feed: RichFeed, page: Int? = null): String {
    return page?.let { actualPage -> "${feed.feedUrl}/atom?page=${actualPage}" }
      ?: feed.feedUrl
  }
}
