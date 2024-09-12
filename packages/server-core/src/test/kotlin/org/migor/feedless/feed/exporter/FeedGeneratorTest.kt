package org.migor.feedless.feed.exporter

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.parser.XmlFeedParser
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.util.JsonUtil
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class FeedGeneratorTest {

  val corrId = "test"

  @Test
  @Disabled
  fun `generated feed and parsed feed is identical`() = runTest {
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
    feed.publishedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    feed.links = emptyList()
    feed.feedUrl = url
    feed.expired = false
    feed.tags = listOf("tag1", "tag2")

    val item = JsonItem()
    item.id = "id"
    item.title = "title"
    item.url = "http://item-url"
    item.tags = emptyList()
//    item.authors = emptyList()
    item.text = "contentText"
    item.rawBase64 = "base64-image-data"
    item.rawMimeType = "image/png"
    item.html = "<div>foo</div>"
//    item.imageUrl = "imageUrl"
//    item.bannerImage = "bannerImage"
//    item.language = "language"
//    item.authors = emptyList()
//    item.attachments = emptyList()
    item.publishedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
//    item.modifiedAt = DateUtils.truncate(Date(), Calendar.MILLISECOND)
//    item.startingAt = Date()
    val latLng = JsonPoint()
    latLng.x = 1.1
    latLng.y = 2.2
    item.latLng = latLng

    feed.items = listOf(item)
    feed.page = 104


    val exporter = SyndAtomFeedExporter()
//    exporter.commit = "foo-commit-id"
    val atom = exporter.toAtom(corrId, feed)
    val actualFeed = XmlFeedParser().process(
      corrId, HttpResponse(
        contentType = "application/xml",
        url = url,
        statusCode = 200,
        responseBody = atom.toByteArray(),
      )
    )

    actualFeed.id = ""
    feed.id = ""
    actualFeed.items.forEach { it.authors = emptyList() }
    feed.items.forEach { it.authors = emptyList() }
    actualFeed.items.forEach { it.id = "" }
    feed.items.forEach { it.id = "" }
    val actualFeedMap = JsonUtil.gson.fromJson(JsonUtil.gson.toJson(actualFeed), Map::class.java)
    val expectedFeedMap = JsonUtil.gson.fromJson(JsonUtil.gson.toJson(feed), Map::class.java)

    assertThat(actualFeedMap).isEqualTo(expectedFeedMap)
  }
}
