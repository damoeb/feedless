package org.migor.feedless.feed.exporter

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
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.springframework.stereotype.Service
import java.net.URL


@Service
class SyndAtomFeedExporter {
  fun toAtom(corrId: String, r: RichFeed): String {
    val output = SyndFeedOutput()
    val feed = output.outputString(toSyndFeed(r))
    val endOfHead = feed.indexOf("<feed")
    val xsl = "<?xml-stylesheet href=\"/stream/feed/feed.xsl\" type=\"text/xsl\"?>\n"
    return feed.substring(0, endOfHead) + xsl + feed.substring(endOfHead)
  }

  private fun toSyndFeed(richFeed: RichFeed): SyndFeed {
    val feed = SyndFeedImpl()
//    val atomLinkModule = AtomLinkModuleImpl()
//    val atomLink = AtomLink()
//    atomLink.type = "self"
//    atomLink.href = richFeed.feedUrl
//    atomLink.type = "application/atom+xml"
//    atomLinkModule.link = atomLink
//    feed.modules = listOf(atomLinkModule)

    feed.uri = "https://feedless.org/feed/${richFeed.id}"
    feed.feedType = "atom_1.0"
    feed.title = richFeed.title
    feed.description = richFeed.description
//    if (StringUtils.isNoneBlank(richFeed.author)) {
//      feed.author = richFeed.author
//    }
    feed.image = richFeed.imageUrl?.let { toSyndImage(it) }
    richFeed.language?.let {
      feed.language = it
    }
    feed.publishedDate = richFeed.publishedAt
    val link = SyndLinkImpl()
    link.type = "self"
    link.href = richFeed.feedUrl
    link.type = "application/atom+xml"
    val website = SyndLinkImpl()
    website.type = "alternate"
    website.href = richFeed.websiteUrl
    website.type = "text/html"
    feed.links = listOf(link, website)

    feed.entries = richFeed.items.map { toSyndEntry(it) }
    return feed
  }

  private fun toSyndEntry(article: RichArticle): SyndEntry {
    val entry = SyndEntryImpl()

    entry.uri = "https://feedless.org/articles/${article.id}"
    entry.title = article.title
    entry.categories = (article.tags ?: emptyList()).map { toSyndCategory(it) }
    entry.contents = toSyndContents(article)
    entry.description = toSyndContentPlain(article)
//    entry.enclosures = listOf() // it.imageUrl = imageUrl
    entry.link = article.url
//    if (StringUtils.isNoneBlank(article.author)) {
//      entry.author = article.author
//    }
    entry.author = URL(article.url).host
    entry.enclosures = article.attachments.map { toSyndEnclosure(it) }
    entry.publishedDate = article.publishedAt

    return entry
  }


  private fun toSyndContentPlain(article: RichArticle): SyndContent {
    val plain = SyndContentImpl()
    plain.value = if (StringUtils.isBlank(article.contentText)) {
      article.contentRawMime
    } else {
      article.contentText
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

  private fun toSyndContents(it: RichArticle): List<SyndContent> {
    val contents = mutableListOf<SyndContent>()
    if (StringUtils.isNoneBlank(it.contentRaw)) {
      val other = SyndContentImpl()
      other.value = it.contentRaw
      other.type = it.contentRawMime
      contents.add(other)
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
