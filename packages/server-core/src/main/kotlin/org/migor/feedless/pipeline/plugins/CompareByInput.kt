package org.migor.feedless.pipeline.plugins

import com.google.gson.annotations.SerializedName

data class CompareByInput(
  @SerializedName("fragmentNameRef")
  val fragmentNameRef: String? = null,
  @SerializedName("field")
  val `field`: RecordField,
)
