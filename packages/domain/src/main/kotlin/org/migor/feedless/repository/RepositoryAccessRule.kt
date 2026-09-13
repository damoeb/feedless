package org.migor.feedless.repository

import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment

/** Owner or member of the owning group — the one rule behind [RepositoryGuard] and the /api/v1 access guard. */
object RepositoryAccessRule {

  suspend fun isOwnerOrMember(
    repository: Repository,
    userId: UserId,
    roles: Collection<RoleInGroup> = RoleInGroup.entries,
    memberships: suspend () -> List<UserGroupAssignment>,
  ): Boolean {
    // the owner needs no membership lookup
    if (repository.ownerId == userId) {
      return true
    }
    return memberships().any { it.groupId == repository.groupId && it.role in roles }
  }
}
