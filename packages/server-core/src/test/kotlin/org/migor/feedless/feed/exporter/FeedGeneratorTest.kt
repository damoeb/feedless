package org.migor.feedless.feed.exporter

import org.apache.commons.lang3.time.DateUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.parser.XmlFeedParser
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import java.util.*
import java.util.concurrent.TimeUnit

class FeedGeneratorTest {

  val corrId = "test"

  @Test
  fun `generated feed and parsed feed is identical`() {
    val url = "https://foo.bar"

    val feed = JsonFeed()
    feed.id = "id"
    feed.title = "feed title"
//    expectedFeed.favicon = ""
    feed.description = "description"
//    expectedFeed.authors = emptyList()
    feed.websiteUrl = "http://websiteUrl"
    feed.imageUrl = "http://imageUrl"
    feed.language = "language"
    feed.publishedAt = DateUtils.truncate(Date(), Calendar.MILLISECOND)
    feed.links = emptyList()
    feed.feedUrl = url
    feed.expired = false
    feed.tags = listOf("tag1", "tag2")

    val item = JsonItem()
    item.id= "id"
    item.title= "title"
    item.url= "http://item-url"
    item.tags = emptyList()
//    item.authors = emptyList()
    item.contentText = "contentText"
    item.contentRawBase64 = "base64-image-data"
    item.contentRawMime = "image/png"
    item.contentHtml = "<div>foo</div>"
//    item.imageUrl = "imageUrl"
//    item.bannerImage = "bannerImage"
//    item.language = "language"
//    item.authors = emptyList()
//    item.attachments = emptyList()
    item.publishedAt = DateUtils.truncate(Date(), Calendar.MILLISECOND)
    item.modifiedAt = DateUtils.truncate(Date(), Calendar.MILLISECOND)
//    item.startingAt = Date()
    val latLng = JsonPoint()
    latLng.x = 1.1
    latLng.y = 2.2
    item.latLng = latLng

    feed.items = listOf(item)
    feed.page = 104


    val exporter = SyndAtomFeedExporter()
    val atom = exporter.toAtom(corrId, feed)
    val actualFeed = XmlFeedParser().process(corrId, HttpResponse(
      contentType = "application/xml",
      url = url,
      statusCode = 200,
      responseBody = atom.toByteArray(),
    )
    )
    assertThat(actualFeed).isEqualTo(feed)
  }
}
