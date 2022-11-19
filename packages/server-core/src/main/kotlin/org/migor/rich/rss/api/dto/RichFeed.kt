package org.migor.rich.rss.api.dto

import com.google.gson.annotations.SerializedName
import java.util.*

data class RichFeed(
  val id: String?,
  val title: String?,
  val description: String?,
  val author: String? = null,
  @SerializedName(value = "home_page_url")
  val home_page_url: String?,
  val image_url: String? = null,
  val language: String? = null,
  @SerializedName(value = "date_published")
  var date_published: Date?,
  var items: List<RichArticle>,
  @SerializedName(value = "feed_url")
  var feed_url: String? = null,
  val expired: Boolean = false,
  val lastPage: Int? = null,
  val selfPage: Int? = null,
  val tags: List<String>? = null,
) {
  var previous_url: String? = null
  var next_url: String? = null
  var last_url: String? = null
}
