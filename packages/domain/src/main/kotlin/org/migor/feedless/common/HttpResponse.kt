package org.migor.feedless.common

import java.io.Serializable

data class HttpResponse(
  val contentType: String,
  val url: String,
  val statusCode: Int,
  val responseBody: ByteArray,
) : Serializable

