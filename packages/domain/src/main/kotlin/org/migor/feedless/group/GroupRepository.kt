package org.migor.feedless.group

interface GroupRepository {
  fun findByName(name: String): Group?
  fun findById(groupId: GroupId): Group?
  fun save(group: Group): Group
}
