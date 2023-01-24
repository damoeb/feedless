package org.migor.rich.rss.harvest.feedparser.json

import com.google.gson.annotations.SerializedName

data class JsonAuthor(
  @SerializedName(value = "name")
  val name: String?,
  @SerializedName(value = "url")
  val url: String?,
  @SerializedName(value = "avatar")
  val avatar: String?
)
