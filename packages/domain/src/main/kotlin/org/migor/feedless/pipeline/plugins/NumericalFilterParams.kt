package org.migor.feedless.pipeline.plugins

import com.google.gson.annotations.SerializedName

data class NumericalFilterParams(
  @SerializedName("operator")
  val `operator`: NumberFilterOperator,
  @SerializedName("value")
  val `value`: Int,
)
