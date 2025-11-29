package org.migor.feedless.capability

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.migor.feedless.user.UserId

@Serializable
open class UserCapability(val userId: UserId) : Capability<UserId>(ID, userId) {
  companion object {
    fun fromString(value: String): UserCapability {
      return Json.decodeFromString<UserCapability>(value)
    }

    fun resolve(value: UnresolvedCapability): UserCapability {
      return fromString(value.capabilityPayload)
    }

    val ID: CapabilityId = CapabilityId("user");
  }
}
