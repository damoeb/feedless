package org.migor.rss.rich.harvest
import org.asynchttpclient.BoundRequestBuilder

data class HarvestContext(
  val feedUrl: String,
  val prerender: Boolean = false,
  val expectedStatusCode: Int = 200,
  val prepareRequest: ((BoundRequestBuilder) -> Unit)? = null
)
