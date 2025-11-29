package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName

open class OpenSearchQuery {
  @SerializedName(value = "shortName")
  lateinit var shortName: String
}
