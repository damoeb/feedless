package org.migor.rss.rich.harvest

import org.asynchttpclient.BoundRequestBuilder
import org.migor.rss.rich.database.model.Feed

data class HarvestContext(
  val feedUrl: String,
  val feed: Feed,
  val prerender: Boolean = false,
  val expectedStatusCode: Int = 200,
  val prepareRequest: ((BoundRequestBuilder) -> Unit)? = null
)
