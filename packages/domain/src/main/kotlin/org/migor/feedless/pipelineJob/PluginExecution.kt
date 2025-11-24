package org.migor.feedless.pipelineJob

import com.google.gson.annotations.SerializedName
import org.migor.feedless.actions.PluginExecutionJson

data class PluginExecution(
  @SerializedName("id") val id: String,
  @SerializedName("paramsJsonString") val params: PluginExecutionJson
)
