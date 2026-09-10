package org.migor.feedless.session

import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignmentRepository

/**
 * The group a user token acts in: the user's first group assignment with role [RoleInGroup.owner].
 *
 * Every path that issues a token for a user — SSO login, the `authUser` and magic-mail session logins,
 * and the `createUserSecret` API token — puts this group into the token next to the user, so every
 * request made with it has a `RequestContext.groupId` to write into. A token only ever carries a group
 * the user owns.
 *
 * @throws NoActingGroupException if the user owns no group — no token is issued then, rather than one
 * that crashes on its first write.
 */
fun UserGroupAssignmentRepository.actingGroupOf(userId: UserId): GroupAndRole =
  findAllByUserId(userId)
    .firstOrNull { it.role == RoleInGroup.owner }
    ?.let { GroupAndRole(it.groupId, it.role) }
    ?: throw NoActingGroupException(userId)

/** A token for this user would act in no group, so every group-scoped write made with it would fail. */
class NoActingGroupException(userId: UserId) :
  IllegalStateException("user ${userId.uuid} owns no group, so no token can be issued for it")
