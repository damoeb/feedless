package org.migor.feedless.group

import org.migor.feedless.user.UserId

interface GroupRepository {
  fun findByName(name: String): Group?
  fun findById(groupId: GroupId): Group?
  fun save(group: Group): Group
  fun findAllByOwner(id: UserId): List<Group>
}
