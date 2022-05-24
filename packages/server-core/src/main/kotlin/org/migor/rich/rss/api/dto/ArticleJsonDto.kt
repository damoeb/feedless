package org.migor.rich.rss.api.dto

import com.google.gson.annotations.SerializedName
import com.rometools.rome.feed.module.Module
import java.util.*

data class ArticleJsonDto(
  val id: String,
  val title: String,
  val tags: Collection<String>? = null,
  val content_text: String,
  val content_raw: String?,
  val content_raw_mime: String?,
  @SerializedName("image")
  val main_image_url: String?,
  val url: String,
  val author: String? = null,
  val enclosures: Collection<EnclosureDto>? = null,
  val modules: MutableList<Module>? = null,
  val date_published: Date,
  val commentsFeedUrl: String? = null
)
