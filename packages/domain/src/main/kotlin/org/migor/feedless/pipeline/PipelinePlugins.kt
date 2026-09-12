package org.migor.feedless.pipeline

import kotlin.reflect.KClass

interface PipelinePlugins {
  suspend fun findAll(): List<Plugin>
  suspend fun findById(id: String): Plugin?
  suspend fun <T : Plugin> resolveById(id: String, type: KClass<T>): T?
}

// interfaces cannot declare reified inline members
suspend inline fun <reified T : Plugin> PipelinePlugins.resolveById(id: String): T? = resolveById(id, T::class)
