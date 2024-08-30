package org.migor.feedless.feed.exporter

import com.rometools.modules.itunes.EntryInformationImpl
import com.rometools.modules.itunes.FeedInformationImpl
import com.rometools.modules.mediarss.MediaEntryModuleImpl
import com.rometools.modules.mediarss.types.MediaContent
import com.rometools.modules.mediarss.types.UrlReference
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
import org.migor.feedless.util.JsonUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder


@Service
class SyndAtomFeedExporter {

  @Value("\${APP_GIT_HASH}")
  lateinit var commit: String

  fun toAtom(corrId: String, jsonFeed: JsonFeed): String {
    val output = SyndFeedOutput()
    val xml = output.outputString(toSyndFeed(jsonFeed), true)
    val endOfHead = xml.indexOf("<feed")
    val xsl = "<?xml-stylesheet href=\"/feed/static/feed.xsl\" type=\"text/xsl\"?>\n"
    return xml.substring(0, endOfHead) + xsl + xml.substring(endOfHead)
  }

  private fun toSyndFeed(jsonFeed: JsonFeed): SyndFeed {
    val syndFeed = SyndFeedImpl()
    syndFeed.uri = "https://feedless.org/feed/${jsonFeed.id}"
    syndFeed.feedType = "atom_1.0"
    syndFeed.generator = "feedless.org/$commit"
    syndFeed.title = jsonFeed.title
    syndFeed.description = jsonFeed.description

    val feedInformation = FeedInformationImpl()
    jsonFeed.tags?.let {
      feedInformation.keywords = it.toTypedArray()
      syndFeed.categories = it.map { category ->
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
    syndFeed.modules.add(feedInformation)
//    val imageNamespace = Namespace.getNamespace("image", "http://purl.org/rss/1.0/modules/image/")
//    val element = Element("image", imageNamespace)
//    feed.foreignMarkup.add(element)

    syndFeed.image = jsonFeed.imageUrl?.let { toSyndImage(it) }
    jsonFeed.language?.let {
      syndFeed.language = it
    }
    syndFeed.publishedDate = jsonFeed.publishedAt
    val link = SyndLinkImpl()
    link.rel = "self"

    link.href = UriComponentsBuilder.fromHttpUrl(jsonFeed.feedUrl).queryParam("page", jsonFeed.page).build().toUri().toString()
    link.type = "application/atom+xml"

    val website = SyndLinkImpl()
    website.rel = "alternate"
    website.href = jsonFeed.websiteUrl
    website.type = "text/html"

    val links = mutableListOf(link, website)

    if (!jsonFeed.isLast) {
      val next = SyndLinkImpl()
      next.rel = "next"
      next.href = UriComponentsBuilder.fromHttpUrl(jsonFeed.feedUrl).queryParam("page", jsonFeed.page + 1).build().toUri().toString()
      next.type = "application/atom+xml"
      links.add(next)
    }

    syndFeed.links = links.toList()

    val feedlessModule = FeedlessModuleImpl()
    feedlessModule.setPage(jsonFeed.page)
    syndFeed.modules.add(feedlessModule)

    syndFeed.entries = jsonFeed.items.map { toSyndEntry(it) }
    return syndFeed
  }

  private fun toSyndEntry(jsonItem: JsonItem): SyndEntry {
    val syndEntry = SyndEntryImpl()
    syndEntry.uri = "https://feedless.org/articles/${jsonItem.id}"
    syndEntry.title = jsonItem.title
    syndEntry.categories = (jsonItem.tags ?: emptyList()).map { toSyndCategory(it) }
    syndEntry.contents = toSyndContents(jsonItem)
    syndEntry.description = toSyndDescription(jsonItem)

    val feedlessModule = FeedlessModuleImpl()
    feedlessModule.setStartingAt(jsonItem.startingAt)
    feedlessModule.setData(jsonItem.contentRawBase64)
    feedlessModule.setDataType(jsonItem.contentRawMime)
    jsonItem.latLng?.let {
      feedlessModule.setLatLng(JsonUtil.gson.toJson(it))
    }

    syndEntry.modules.add(feedlessModule)

//    jsonItem.latLng?.let {
//      val geo = W3CGeoModuleImpl()
//      geo.position = Position(it.x, it.y)
////      feedlessModule.setLatLng(JsonUtil.gson.toJson(jsonItem.latLng))
//      syndEntry.modules.add(geo)
//    }

    jsonItem.imageUrl?.let {
      val image = Element("image", Namespace.getNamespace("image", "http://web.resource.org/rss/1.0/modules/image/"))
      image.addContent(jsonItem.imageUrl)
      syndEntry.foreignMarkup.add(image)

      val reference = UrlReference(it)
      val mediaContent = MediaContent(reference)
      val mediaContents = arrayOf(mediaContent)
      val imageModule = MediaEntryModuleImpl()
      imageModule.mediaContents = mediaContents
      syndEntry.modules.add(imageModule)
    }

    val entryInformation = EntryInformationImpl()
    entryInformation.author = jsonItem.authors?.firstOrNull()?.name

    syndEntry.modules.add(entryInformation)

    syndEntry.link = jsonItem.url
//    syndEntry.author = URL(jsonItem.url).host
    syndEntry.enclosures = jsonItem.attachments.map { toSyndEnclosure(it) }
    syndEntry.publishedDate = jsonItem.publishedAt

    return syndEntry
  }

  private fun toSyndDescription(article: JsonItem): SyndContent {
    val syndContent = SyndContentImpl()
    syndContent.value = article.contentText
    syndContent.type = "text/plain"
    return syndContent
  }

  private fun toSyndEnclosure(jsonAttachment: JsonAttachment): SyndEnclosure {
    val syndEnclosure = SyndEnclosureImpl()
    syndEnclosure.url = jsonAttachment.url
    syndEnclosure.type = jsonAttachment.type
    return syndEnclosure
  }

  private fun toSyndCategory(tag: String): SyndCategory {
    val syndCategory = SyndCategoryImpl()
    syndCategory.name = tag
    return syndCategory
  }

  private fun toSyndContents(jsonItem: JsonItem): MutableList<SyndContent> {
    val contents = mutableListOf<SyndContent>()
    if (StringUtils.isNoneBlank(jsonItem.contentHtml)) {
      val htmlContent = SyndContentImpl()
      htmlContent.value = jsonItem.contentHtml
      htmlContent.type = "text/html"
      htmlContent.mode = "encoded"
      contents.add(htmlContent)
    }
    return contents
  }

  private fun toSyndImage(imageUrl: String): SyndImage {
    val syndImage = SyndImageImpl()
    syndImage.url = imageUrl
    return syndImage
  }
}
