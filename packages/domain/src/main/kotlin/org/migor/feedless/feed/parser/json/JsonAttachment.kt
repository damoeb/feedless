package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName
import java.io.Serializable

@kotlinx.serialization.Serializable
open class JsonAttachment(
  @SerializedName("size_in_bytes")
  var length: Long?,
  @SerializedName("mime_type")
  var type: String,
  @SerializedName("url")
  var url: String,
  @SerializedName("duration_in_seconds")
  var duration: Long?
) : Serializable
