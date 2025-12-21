package org.migor.feedless.capability

import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.util.JsonSerializer.fromJson

open class GroupCapability(val group: GroupAndRole) : Capability<GroupAndRole>(ID, group) {
  companion object {
    fun fromString(value: String): GroupAndRole {
      return fromJson(value)
    }

    val ID: CapabilityId = CapabilityId("groups")
  }
}
