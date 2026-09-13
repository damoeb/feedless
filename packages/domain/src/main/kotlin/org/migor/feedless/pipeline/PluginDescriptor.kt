package org.migor.feedless.pipeline

data class PluginDescriptor(
  val id: String,
  val name: String,
  val listed: Boolean,
  val fragmentTransformer: Boolean,
)
