package org.migor.rich.rss.harvest.feedparser.json

import com.google.gson.annotations.SerializedName

open class JsonAttachment {
  @SerializedName("duration_in_seconds")
  var length: Long? = null

  @SerializedName("size_in_bytes")
  var size: Long? = null

  @SerializedName("mime_type")
  lateinit var type: String

  @SerializedName("url")
  lateinit var url: String
}
