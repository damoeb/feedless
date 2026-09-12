package org.migor.feedless.pipeline

interface PipelinePlugins {
  suspend fun findAll(): List<Plugin>
  suspend fun findById(id: String): Plugin?
}
