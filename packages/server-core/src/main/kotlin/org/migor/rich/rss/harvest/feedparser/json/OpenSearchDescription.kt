package org.migor.rich.rss.harvest.feedparser.json

import com.google.gson.annotations.SerializedName

open class OpenSearchDescription {
  @SerializedName(value = "shortName")
  lateinit var shortName: String

  @SerializedName(value = "description")
  lateinit var description: String

  @SerializedName(value = "contact")
  lateinit var contact: String

  @SerializedName(value = "tags")
  lateinit var tags: String

  @SerializedName(value = "language")
  lateinit var language: String

  @SerializedName(value = "inputEncoding")
  lateinit var inputEncoding: String

  @SerializedName(value = "outputEncoding")
  lateinit var outputEncoding: String

  @SerializedName(value = "longName")
  lateinit var longName: String
  //  autodiscovery
//  <link rel="search"
//  href="http://example.com/opensearchdescription.xml"
//  type="application/opensearchdescription+xml"
//  title="Content Search" />
}
