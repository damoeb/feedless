package org.migor.rss.rich.api.dto

import com.google.gson.annotations.SerializedName
import java.util.*

data class FeedJsonDto(
  val id: String?,
  @SerializedName(value = "title")
  val name: String?,
  val description: String?,
  val home_page_url: String?,
  var date_published: Date?,
  var items: List<ArticleJsonDto?>? = null,
  var feed_url: String? = null,
  val expired: Boolean,
  val lastPage: Int? = null,
  val selfPage: Int? = null
)
