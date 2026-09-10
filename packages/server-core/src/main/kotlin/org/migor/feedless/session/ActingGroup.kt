package org.migor.feedless.session

import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository

/**
 * The group a user token acts in: the oldest group the user holds an [RoleInGroup.owner] assignment in.
 *
 * Every path that issues a token for a user — SSO login, the `authUser` and magic-mail session logins,
 * and the `createUserSecret` API token — puts this group into the token next to the user, so every
 * request made with it has a `RequestContext.groupId` to write into. A token only ever carries a group
 * the user owns, and [ownsGroup] re-checks that on every request ([TokenAuthenticator]).
 *
 * @throws NoActingGroupException if the user owns no group — no token is issued then, rather than one
 * that fails on its first write.
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
