package org.migor.feedless.pipeline.plugins

import com.google.gson.annotations.SerializedName

data class StringFilterParams(
  @SerializedName("operator")
  val `operator`: StringFilterOperator,
  @SerializedName("value")
  val `value`: String,
)
