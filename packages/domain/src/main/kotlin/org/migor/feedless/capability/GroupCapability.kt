package org.migor.feedless.capability

import org.migor.feedless.group.GroupId

open class GroupCapability(groupsIds: List<GroupId>) : Capability<List<GroupId>>(ID, groupsIds) {
  companion object {
    val ID: CapabilityId = CapabilityId("groups")
  }
}
