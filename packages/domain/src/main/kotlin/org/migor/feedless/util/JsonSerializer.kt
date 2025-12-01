package org.migor.feedless.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.util.*

object JsonSerializer {
  val log = LoggerFactory.getLogger(JsonSerializer::class.simpleName)

  val json = Json {
    serializersModule = SerializersModule {
      contextual(UUID::class, UUIDSerializer)
      contextual(LocalDateTime::class, LocalDateTimeSerializer)
      contextual(File::class, FileSerializer)
    }
    encodeDefaults = true
    prettyPrint = true
  }

  inline fun <reified T> toJson(obj: T): String {
    return json.encodeToString(obj)
  }

  inline fun <reified T> fromJson(jsonString: String): T {
    try {
      return json.decodeFromString(jsonString)
    } catch (e: Exception) {
      log.error("Can't parse json: $jsonString", e)
      throw RuntimeException("Failed to convert $jsonString to object", e)
    }
  }
}
