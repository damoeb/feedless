package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class JsonAttachment : Serializable {
  @SerializedName("size_in_bytes")
  var size: Long? = null

  @SerializedName("duration_in_seconds")
  var duration: Long? = null

  @SerializedName("mime_type")
  lateinit var type: String

  @SerializedName("url")
  lateinit var url: String
}
