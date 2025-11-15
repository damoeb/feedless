package org.migor.feedless.pipeline.plugins

import com.google.gson.annotations.SerializedName

data class StringFilter(
  @SerializedName("eq") val eq: String? = null,
  @SerializedName("in") val `in`: List<String>? = null,
)
