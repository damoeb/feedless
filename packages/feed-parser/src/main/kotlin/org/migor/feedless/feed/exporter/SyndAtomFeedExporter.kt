package org.migor.feedless.feed.exporter

import com.rometools.modules.atom.modules.AtomLinkModuleImpl
import com.rometools.modules.itunes.EntryInformationImpl
import com.rometools.modules.itunes.FeedInformationImpl
import com.rometools.modules.mediarss.MediaEntryModuleImpl
import com.rometools.modules.mediarss.types.MediaContent
import com.rometools.modules.mediarss.types.UrlReference
import com.rometools.rome.feed.atom.Link
import com.rometools.rome.feed.synd.SyndCategory
import com.rometools.rome.feed.synd.SyndCategoryImpl
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEnclosureImpl
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndFeedImpl
import com.rometools.rome.feed.synd.SyndImage
import com.rometools.rome.feed.synd.SyndImageImpl
import com.rometools.rome.feed.synd.SyndLinkImpl
import com.rometools.rome.io.SyndFeedOutput
import org.apache.commons.lang3.StringUtils
import org.jdom2.Element
import org.jdom2.Namespace
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.util.toLegacyDate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class SyndAtomFeedExporter {

  private val log = LoggerFactory.getLogger(SyndAtomFeedExporter::class.simpleName)

  fun toAtom(r: JsonFeed): String {
    val output = SyndFeedOutput()
    val feed = output.outputString(toSyndFeed(r))
    val endOfHead = feed.indexOf("<feed")
    val xsl = "<?xml-stylesheet href=\"/feed/static/feed.xsl\" type=\"text/xsl\"?>\n"
    return feed.substring(0, endOfHead) + xsl + feed.substring(endOfHead)
  }

  private fun toSyndFeed(richFeed: JsonFeed): SyndFeed {
    val feed = SyndFeedImpl()
    val atomLinkModule = AtomLinkModuleImpl()
    val atomLink = Link()
    atomLink.rel = "self"
    atomLink.href = richFeed.feedUrl
    atomLink.type = "application/atom+xml"
    atomLinkModule.link = atomLink
    feed.modules.add(atomLinkModule)

    feed.uri = "https://feedless.org/feed/${richFeed.id}"
    feed.feedType = "atom_1.0"
    feed.title = richFeed.title
    feed.description = richFeed.description

    val feedInformation = FeedInformationImpl()
    richFeed.tags?.let {
      feedInformation.keywords = it.toTypedArray()
    }
    richFeed.authors?.let {
      feedInformation.author = it.firstOrNull()?.name
    }
    feed.modules.add(feedInformation)
//    val imageNamespace = Namespace.getNamespace("image", "http://purl.org/rss/1.0/modules/image/")
//    val element = Element("image", imageNamespace)
//    feed.foreignMarkup.add(element)

    feed.image = richFeed.imageUrl?.let { toSyndImage(it) }
    richFeed.language?.let {
      feed.language = it
    }
    feed.publishedDate = richFeed.publishedAt.toLegacyDate()
    val link = SyndLinkImpl()
    link.rel = "self"
    link.href = richFeed.feedUrl
    link.type = "application/atom+xml"
    val website = SyndLinkImpl()
    website.rel = "alternate"
    website.href = richFeed.websiteUrl
    website.type = "text/html"
    feed.links = listOf(link, website)

    feed.entries = richFeed.items.map { toSyndEntry(it) }
    return feed
  }

  private fun toSyndEntry(article: JsonItem): SyndEntry {
    val entry = SyndEntryImpl()
    entry.uri = "https://feedless.org/articles/${article.id}"
    entry.title = article.title
    entry.categories = (article.tags ?: emptyList()).map { toSyndCategory(it) }
    entry.contents = toSyndContents(article)
    entry.description = toSyndContentPlain(article)

    article.imageUrl?.let {
      val image = Element("image", Namespace.getNamespace("image", "http://web.resource.org/rss/1.0/modules/image/"))
      image.addContent(article.imageUrl)
      entry.foreignMarkup.add(image)

      val reference = UrlReference(it)
      val mediaContent = MediaContent(reference)
      val mediaContents = arrayOf(mediaContent)
      val imageModule = MediaEntryModuleImpl()
      imageModule.mediaContents = mediaContents
      entry.modules.add(imageModule)
    }

    val entryInformation = EntryInformationImpl()
    entryInformation.author = article.authors?.firstOrNull()?.name

    entry.modules.add(entryInformation)

    entry.link = article.url

    entry.enclosures = article.attachments.map { toSyndEnclosure(it) }
    entry.publishedDate = article.publishedAt.toLegacyDate()

    return entry
  }


  private fun toSyndContentPlain(article: JsonItem): SyndContent {
    val plain = SyndContentImpl()
    plain.value = if (StringUtils.isBlank(article.text)) {
      article.rawMimeType
    } else {
      article.text
    }
    plain.type = "text/plain"
    return plain
  }

  private fun toSyndEnclosure(it: JsonAttachment): SyndEnclosure {
    val e = SyndEnclosureImpl()
    e.url = it.url
    e.type = it.type
    return e
  }

  private fun toSyndCategory(it: String): SyndCategory {
    val c = SyndCategoryImpl()
    c.name = it
    return c
  }

  private fun toSyndContents(it: JsonItem): List<SyndContent> {
    val contents = mutableListOf<SyndContent>()
    if (StringUtils.isNoneBlank(it.html)) {
      val htmlContent = SyndContentImpl()
      htmlContent.value = it.html
      htmlContent.type = "text/html"
      htmlContent.mode = "encoded"
      contents.add(htmlContent)
    }

    contents.add(toSyndContentPlain(it))

    return contents
  }

  private fun toSyndImage(imageUrl: String): SyndImage {
    val image = SyndImageImpl()
    image.url = imageUrl
    return image
  }
}
