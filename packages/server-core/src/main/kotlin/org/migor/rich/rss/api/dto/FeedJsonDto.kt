package org.migor.rich.rss.api.dto

import com.google.gson.annotations.SerializedName
import java.util.*

data class FeedJsonDto(
  val id: String?,
  @SerializedName(value = "title")
  val name: String?,
  val description: String?,
  val home_page_url: String?,
  val icon: String? = null,
  var date_published: Date?,
  var items: Collection<ArticleJsonDto?>? = null,
  var feed_url: String? = null,
  val expired: Boolean,
  val lastPage: Int? = null,
  val selfPage: Int? = null,
  val tags: List<String>? = null
) {
  var previous_url: String? = null
  var next_url: String? = null
  var last_url: String? = null
}
