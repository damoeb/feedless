package org.migor.feedless.document

data class DocumentUpdate(
  val title: String? = null,
  val text: String? = null,
  val url: String? = null,
  val tags: List<String>? = null,
  val rawBase64: String? = null,
  val rawMimeType: String? = null,
)
