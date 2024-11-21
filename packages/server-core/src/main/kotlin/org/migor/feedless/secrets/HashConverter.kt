package org.migor.feedless.secrets

import jakarta.persistence.AttributeConverter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class HashConverter : AttributeConverter<String, String> {
  private val prefix = "bcrypt:"

  override fun convertToDatabaseColumn(value: String): String {
    return if (value.startsWith(prefix)) {
      value
    } else {
      val encoder = BCryptPasswordEncoder()
      return "$prefix${encoder.encode(value)}"
    }
  }

  override fun convertToEntityAttribute(value: String): String {
    return value.replaceFirst(prefix, "")
  }
}
