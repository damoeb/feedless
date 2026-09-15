package org.migor.feedless.common

interface HttpFetcher {
  suspend fun httpGet(url: String, expectedHttpStatus: Int, headers: Map<String, String>? = null): HttpResponse
}
