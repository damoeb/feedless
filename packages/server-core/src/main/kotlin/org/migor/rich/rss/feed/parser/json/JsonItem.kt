package org.migor.rich.rss.feed.parser.json

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

open class JsonItem : GenericFeedItem()

open class GenericFeedItem: Serializable {
  @SerializedName("id")
  lateinit var id: String

  @SerializedName("title")
  lateinit var title: String

  @SerializedName("url")
  lateinit var url: String

  @SerializedName("tags")
  var tags: List<String>? = null

  @SerializedName("content_text")
  lateinit var contentText: String

  @SerializedName("content_html")
  open var contentHtml: String? = null

  @SerializedName("summary")
  var summary: String? = null

  @SerializedName("image")
  var imageUrl: String? = null

  @SerializedName("banner_image")
  var bannerImage: String? = null

  @SerializedName("language")
  var language: String? = null

  @SerializedName("author")
  var author: JsonAuthor? = null

  @SerializedName("authors")
  var authors: List<JsonAuthor>? = null

  @SerializedName("attachments")
  var attachments: List<JsonAttachment> = emptyList()

  @SerializedName("date_published")
  lateinit var publishedAt: Date

  @SerializedName("date_modified")
  var modifiedAt: Date? = null

  @SerializedName("date_starting")
  var startingAt: Date? = null
  var commentsFeedUrl: String? = null
}
