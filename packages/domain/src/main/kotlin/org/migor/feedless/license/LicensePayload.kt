package org.migor.feedless.license

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.migor.feedless.Vertical
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit


@Serializable
data class LicensePayload(
  @SerialName("v") val version: Int,
  @SerialName("n") val name: String,
  @SerialName("e") val email: String,
  @SerialName("c") val createdAt: LocalDateTime,
  @SerialName("u") val validUntil: LocalDateTime? = null,
  @SerialName("s") val scope: Vertical
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LicensePayload

    val trunc = { date: LocalDateTime? ->
      date
        ?.atZone(ZoneOffset.UTC)
        ?.toLocalDateTime()
        ?.truncatedTo(ChronoUnit.DAYS)
    }

    if (name != other.name) return false
    if (email != other.email) return false
    if (trunc(createdAt) != trunc(other.createdAt)
    ) return false

    return scope == other.scope
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + email.hashCode()
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + scope.hashCode()
    return result
  }
}

