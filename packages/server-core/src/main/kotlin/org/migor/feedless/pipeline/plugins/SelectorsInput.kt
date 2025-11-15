package org.migor.feedless.pipeline.plugins

data class SelectorsInput(
  val contextXPath: String,
  val dateIsStartOfEvent: Boolean,
  val dateXPath: String,
  val paginationXPath: String,
  val extendContext: ExtendContentOptions,
  val linkXPath: String,
)
