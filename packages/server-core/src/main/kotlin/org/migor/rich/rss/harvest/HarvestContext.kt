package org.migor.rich.rss.harvest

import org.asynchttpclient.BoundRequestBuilder
import org.migor.rich.rss.database.model.Feed

data class HarvestContext(
  val feedUrl: String,
  val feed: Feed,
  val prerender: Boolean = false,
  val expectedStatusCode: Int = 200,
  val prepareRequest: ((BoundRequestBuilder) -> Unit)? = null
)
