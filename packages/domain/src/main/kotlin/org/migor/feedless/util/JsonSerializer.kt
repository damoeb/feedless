package org.migor.feedless.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDateTime
import java.util.*

val module = SerializersModule {
  contextual(UUID::class, UUIDSerializer)
}

object JsonSerializer {
  val json = Json {
    serializersModule = SerializersModule {
      contextual(UUID::class, UUIDSerializer)
      contextual(LocalDateTime::class, LocalDateTimeSerializer)
    }
    encodeDefaults = true
    prettyPrint = true
  }

  // Serialize any @Serializable object to JSON string
  inline fun <reified T> toJson(obj: T): String {
    return json.encodeToString(obj)
  }

  // Deserialize JSON string to any @Serializable object
  inline fun <reified T> fromJson(jsonString: String): T {
    return json.decodeFromString(jsonString)
  }
}
