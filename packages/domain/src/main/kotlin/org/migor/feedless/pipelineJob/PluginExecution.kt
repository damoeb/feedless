package org.migor.feedless.pipelineJob

import org.migor.feedless.actions.PluginExecutionJson

data class PluginExecution(
  val id: String,
  val params: PluginExecutionJson
)
