package org.migor.feedless.pipeline.plugins

data class DiffRecordsParamsInput(
  val nextItemMinIncrement: Double,
  val compareBy: CompareByInput,
  val inlineDiffImage: Boolean? = null,
  val inlineLatestImage: Boolean? = null,
  val inlinePreviousImage: Boolean? = null,
)
