package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.util.*

open class JsonFeed: java.io.Serializable {
  @SerializedName(value = "id")
  lateinit var id: String

  @SerializedName(value = "title")
  lateinit var title: String

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

  @SerializedName(value = "user_comment")
  var user_comment: String? = null

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

//  @SerializedName(value = "edit_url")
//  var editUrl: String? = null

  @SerializedName(value = "expired")
  var expired: Boolean = false

  //    @SerializedName(value = "home_page_url")
//    val lastPage: Int? = null,
//    @SerializedName(value = "feed_url")
//    val selfPage: Int? = null,
  @SerializedName(value = "tags")
  var tags: List<String>? = null

  @Transient
  var previousUrl: String? = null

  @Transient
  var nextUrl: String? = null
}
