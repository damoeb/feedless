package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName
import java.util.*

open class JsonFeed: java.io.Serializable {
  @SerializedName(value = "id")
  lateinit var id: String

  @SerializedName(value = "title")
  lateinit var title: String

  @SerializedName(value = "page")
  var page: Int = 0

  @SerializedName(value = "icon")
  var iconUrl: String? = null

  @SerializedName(value = "version")
  var version: String = "1.1+"

  @SerializedName(value = "favicon")
  var favicon: String? = null

  @SerializedName(value = "description")
  var description: String? = null

  @SerializedName(value = "authors")
  var authors: List<JsonAuthor>? = null

  @SerializedName(value = "home_page_url")
  var websiteUrl: String? = null

//  @SerializedName(value = "user_comment")
//  var user_comment: String? = null

  @SerializedName(value = "imagee")
  var imageUrl: String? = null

  @SerializedName(value = "language")
  var language: String? = null

  @SerializedName(value = "date_published")
  lateinit var publishedAt: Date

  @SerializedName(value = "items")
  lateinit var items: List<JsonItem>

  @SerializedName(value = "feed_url")
  lateinit var feedUrl: String

  @SerializedName(value = "expired")
  var expired: Boolean = false

  @SerializedName(value = "tags")
  var tags: List<String>? = null

  @SerializedName(value = "links")
  var links: List<String>? = null

  @Transient
  var isLast: Boolean = true

}
