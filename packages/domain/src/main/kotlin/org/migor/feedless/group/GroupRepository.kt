package org.migor.feedless.group

interface GroupRepository {
  suspend fun findByName(name: String): Group?
  suspend fun findById(groupId: GroupId): Group?
}
