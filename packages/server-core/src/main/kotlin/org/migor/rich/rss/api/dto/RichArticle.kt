package org.migor.rich.rss.api.dto

import com.google.gson.annotations.SerializedName
import java.util.*

data class RichArticle(
  val id: String,
  val title: String,
  val tags: List<String>? = null,
  @SerializedName("content_text")
  val contentText: String,
  @SerializedName("content_raw")
  val contentRaw: String? = null,
  @SerializedName("content_raw_mime")
  val contentRawMime: String? = null,
  @SerializedName("image")
  val imageUrl: String? = null,
  val url: String,
  val author: String? = null,
  val enclosures: Collection<RichEnclosure>? = null,
  @SerializedName("date_published")
  val publishedAt: Date,
  val commentsFeedUrl: String? = null
)
