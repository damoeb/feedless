package org.migor.feedless.session

import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository

/**
 * The group every issued token acts in: the user's oldest owned group, re-checked per request by [ownsGroup].
 * Throws [NoActingGroupException] rather than issue a token that fails on its first write.
 */
fun UserGroupAssignmentRepository.actingGroupOf(userId: UserId): GroupAndRole =
  findAllByUserId(userId)
    .filter { it.role == RoleInGroup.owner }
    .minWithOrNull(creationOrder)
    ?.let { GroupAndRole(it.groupId, it.role) }
    ?: throw NoActingGroupException.forIssuance(userId)

/** Whether [userId] still holds an [RoleInGroup.owner] assignment in [groupId] — the issuance rule, per request. */
fun UserGroupAssignmentRepository.ownsGroup(userId: UserId, groupId: GroupId): Boolean =
  findByUserIdAndGroupId(userId, groupId)?.role == RoleInGroup.owner

// Oldest first, like findAllByGroupIdOrderByCreatedAtAscIdAsc: createdAt can tie, so the id breaks it.
// The id compares as its canonical string, which orders the way Postgres orders uuid.
private val creationOrder = compareBy<UserGroupAssignment>({ it.createdAt }, { it.id.uuid.toString() })
