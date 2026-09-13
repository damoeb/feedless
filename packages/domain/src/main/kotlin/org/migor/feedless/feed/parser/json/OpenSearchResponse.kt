package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName

open class OpenSearchResponse {
  @SerializedName(value = "totalResults")
  lateinit var totalResults: String

  @SerializedName(value = "startIndex")
  lateinit var startIndex: String

  @SerializedName(value = "itemsPerPage")
  lateinit var itemsPerPage: String

  @SerializedName(value = "query")
  lateinit var query: OpenSearchQuery

//  <link rel="alternate" href="http://example.com/New+York+History?pw=3" type="text/html"/>
//  <link rel="self" href="http://example.com/New+York+History?pw=3&amp;format=atom" type="application/atom+xml"/>
//  <link rel="first" href="http://example.com/New+York+History?pw=1&amp;format=atom" type="application/atom+xml"/>
//  <link rel="previous" href="http://example.com/New+York+History?pw=2&amp;format=atom" type="application/atom+xml"/>
//  <link rel="next" href="http://example.com/New+York+History?pw=4&amp;format=atom" type="application/atom+xml"/>
//  <link rel="last" href="http://example.com/New+York+History?pw=42299&amp;format=atom" type="application/atom+xml"/>
//  <link rel="search" type="application/opensearchdescription+xml" href="http://example.com/opensearchdescription.xml"/>
}
