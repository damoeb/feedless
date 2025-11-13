package org.migor.feedless.license

import com.google.gson.annotations.SerializedName
import org.migor.feedless.Vertical
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

data class LicensePayload(
  @SerializedName("v") val version: Int,
  @SerializedName("n") val name: String,
  @SerializedName("e") val email: String,
  @SerializedName("c") val createdAt: LocalDateTime,
  @SerializedName("u") val validUntil: LocalDateTime? = null,
  @SerializedName("s") val scope: Vertical
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

