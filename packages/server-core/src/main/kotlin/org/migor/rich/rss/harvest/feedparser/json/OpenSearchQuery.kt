package org.migor.rich.rss.harvest.feedparser.json

import com.google.gson.annotations.SerializedName

open class OpenSearchQuery {
  @SerializedName(value = "shortName")
  lateinit var shortName: String
}
