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
import org.springframework.stereotype.Service
import java.net.URL


@Service
class SyndAtomFeedExporter {
  fun toAtom(corrId: String, r: JsonFeed): String {
    val output = SyndFeedOutput()
    val feed = output.outputString(toSyndFeed(r))
    val endOfHead = feed.indexOf("<feed")
    val xsl = "<?xml-stylesheet href=\"/feed/static/feed.xsl\" type=\"text/xsl\"?>\n"
    return feed.substring(0, endOfHead) + xsl + feed.substring(endOfHead)
  }

  private fun toSyndFeed(jsonFeed: JsonFeed): SyndFeed {
    val feed = SyndFeedImpl()
    val atomLinkModule = AtomLinkModuleImpl()
    val atomLink = Link()
    atomLink.rel = "self"
    atomLink.href = jsonFeed.feedUrl
    atomLink.type = "application/atom+xml"
    atomLinkModule.link = atomLink
    feed.modules.add(atomLinkModule)

    feed.uri = "https://feedless.org/feed/${jsonFeed.id}"
    feed.feedType = "atom_1.0"
    feed.title = jsonFeed.title
    feed.description = jsonFeed.description

    val feedInformation = FeedInformationImpl()
    jsonFeed.tags?.let {
      feedInformation.keywords = it.toTypedArray()
      feed.categories = it.map { category ->
        run {
          val c = SyndCategoryImpl()
          c.name = category
          c
        }
      }
    }
    jsonFeed.authors?.let {
      feedInformation.author = it.firstOrNull()?.name
    }
    feed.modules.add(feedInformation)
//    val imageNamespace = Namespace.getNamespace("image", "http://purl.org/rss/1.0/modules/image/")
//    val element = Element("image", imageNamespace)
//    feed.foreignMarkup.add(element)

    feed.image = jsonFeed.imageUrl?.let { toSyndImage(it) }
    jsonFeed.language?.let {
      feed.language = it
    }
    feed.publishedDate = jsonFeed.publishedAt
    val link = SyndLinkImpl()
    link.rel = "self"
    link.href = jsonFeed.feedUrl
    link.type = "application/atom+xml"

    val website = SyndLinkImpl()
    website.rel = "alternate"
    website.href = jsonFeed.websiteUrl
    website.type = "text/html"

    val links = mutableListOf(link, website)

    jsonFeed.nextUrl?.let {
      val next = SyndLinkImpl()
      next.rel = "next"
      next.href = jsonFeed.nextUrl
      next.type = "application/atom+xml"
    }

    jsonFeed.previousUrl?.let {
      val previous = SyndLinkImpl()
      previous.rel = "previous"
      previous.href = jsonFeed.previousUrl
      previous.type = "application/atom+xml"
    }

    feed.links = links.toList()

    feed.entries = jsonFeed.items.map { toSyndEntry(it) }
    return feed
  }

  private fun toSyndEntry(article: JsonItem): SyndEntry {
    val entry = SyndEntryImpl()
    entry.uri = "https://feedless.org/articles/${article.id}"
    entry.title = article.title
    entry.categories = (article.tags ?: emptyList()).map { toSyndCategory(it) }
    entry.contents = toSyndContents(article)
    entry.description = toSyndDescription(article)

    val feedlessModule = FeedlessModuleImpl()
    feedlessModule.setStartingAt(article.startingAt)
    feedlessModule.setLatLng(article.latLng)
    entry.modules.add(feedlessModule)


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
//    article.contentHtml?.let {
////      val module: ContentModule = ContentModuleImpl()
////
////      val item = ContentItem()
////      item.contentFormat = "text/html"
////      item.contentValue = it
////      module.contentItems = listOf(item)
////      entry.modules.add(module)
//      val module: ContentModule = ContentModuleImpl()
//      module.encodeds = listOf(it)
//      entry.modules.add(module)
//    }

    entry.link = article.url
    entry.author = URL(article.url).host
    entry.enclosures = article.attachments.map { toSyndEnclosure(it) }
    entry.publishedDate = article.publishedAt

    return entry
  }

  private fun toSyndDescription(article: JsonItem): SyndContent {
    val content = SyndContentImpl()
    if (StringUtils.isBlank(article.contentHtml)) {
      content.value = article.contentText
      content.type = "text/plain"
    } else {
      content.value = article.contentHtml
      content.type = "text/html"
    }

    return content
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
    if (StringUtils.isNoneBlank(it.contentRawBase64)) {
      val other = SyndContentImpl()
      other.value = it.contentRawBase64
      other.type = it.contentRawMime
      contents.add(other)
    }

    if (StringUtils.isNoneBlank(it.contentHtml)) {
      val other = SyndContentImpl()
      other.value = it.contentHtml
      other.type = "text/html"
      other.mode = "encoded"
      contents.add(other)
    }

//    contents.add(toSyndDescription(it))

    return contents
  }

  private fun toSyndImage(imageUrl: String): SyndImage {
    val image = SyndImageImpl()
    image.url = imageUrl
    return image
  }
}
