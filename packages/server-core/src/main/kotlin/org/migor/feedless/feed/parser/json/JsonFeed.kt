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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JsonFeed) return false

//    if (id != other.id) return false
    if (title != other.title) return false
    if (page != other.page) return false
    if (iconUrl != other.iconUrl) return false
    if (version != other.version) return false
    if (favicon != other.favicon) return false
    if (description != other.description) return false
    if (authors != other.authors) return false
    if (websiteUrl != other.websiteUrl) return false
    if (imageUrl != other.imageUrl) return false
    if (language != other.language) return false
//    if (publishedAt != other.publishedAt) return false
    if (items != other.items) return false
    if (feedUrl != other.feedUrl) return false
    if (expired != other.expired) return false
    if (tags != other.tags) return false
    if (links != other.links) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + title.hashCode()
    result = 31 * result + page
    result = 31 * result + (iconUrl?.hashCode() ?: 0)
    result = 31 * result + version.hashCode()
    result = 31 * result + (favicon?.hashCode() ?: 0)
    result = 31 * result + (description?.hashCode() ?: 0)
    result = 31 * result + (authors?.hashCode() ?: 0)
    result = 31 * result + (websiteUrl?.hashCode() ?: 0)
    result = 31 * result + (imageUrl?.hashCode() ?: 0)
    result = 31 * result + (language?.hashCode() ?: 0)
    result = 31 * result + publishedAt.hashCode()
    result = 31 * result + items.hashCode()
    result = 31 * result + feedUrl.hashCode()
    result = 31 * result + expired.hashCode()
    result = 31 * result + (tags?.hashCode() ?: 0)
    result = 31 * result + (links?.hashCode() ?: 0)
    return result
  }


}
