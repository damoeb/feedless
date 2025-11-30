package org.migor.feedless.capability

import kotlinx.serialization.Serializable
import org.migor.feedless.user.UserId
import org.migor.feedless.util.JsonSerializer.fromJson

@Serializable
open class UserCapability(val userId: UserId) : Capability<UserId>(ID, userId) {
  companion object {
    fun fromString(value: String): UserCapability {
      return fromJson<UserCapability>(value)
    }

    fun resolve(value: UnresolvedCapability): UserCapability {
      return fromString(value.capabilityPayload)
    }

    val ID: CapabilityId = CapabilityId("user");
  }
}
