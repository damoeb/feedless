package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class JsonAuthor(
  @SerializedName(value = "name")
  val name: String?,
  @SerializedName(value = "url")
  val url: String? = null,
  @SerializedName(value = "avatar")
  val avatar: String? = null,
  @SerializedName(value = "email")
  val email: String? = null
) : Serializable
