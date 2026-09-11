package org.migor.feedless.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ConflictException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.user.UserId
import org.migor.feedless.user.userId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class GroupUseCase(
  private val userGroupAssignmentRepository: UserGroupAssignmentRepository,
  private val groupGuard: GroupGuard,
  private val groupRepository: GroupRepository,
  private val repositoryRepository: RepositoryRepository,
  private val transactionTemplate: TransactionTemplate,
) : GroupUseCasePort {

  private val log = LoggerFactory.getLogger(GroupUseCase::class.simpleName)

  override suspend fun create(name: String): Group = withContext(Dispatchers.IO) {
    val ownerId = coroutineContext.userId()
    log.info("create group name=$name ownerId=$ownerId")
    val group = groupRepository.save(Group(name = name, ownerId = ownerId))
    userGroupAssignmentRepository.save(
      UserGroupAssignment(userId = ownerId, groupId = group.id, role = RoleInGroup.owner),
    )
    group
  }

  override suspend fun findByIdForUser(groupId: GroupId): Group? = withContext(Dispatchers.IO) {
    log.info("findByIdForUser groupId=$groupId")
    val group = groupRepository.findById(groupId) ?: return@withContext null
    userGroupAssignmentRepository.findByUserIdAndGroupId(coroutineContext.userId(), groupId)
      ?: throw PermissionDeniedException("not a member")
    group
  }

  /**
   * Deletes the group and its member assignments in one transaction. Every login and every new token
   * needs a group the user owns ([org.migor.feedless.session.actingGroupOf]), so the delete is refused
   * — with nothing changed — when it would leave a member without one, and when the group still owns
   * repositories (their `group_id` is NOT NULL, so the database would refuse it half-way anyway).
   */
  override suspend fun delete(groupId: GroupId) = withContext(Dispatchers.IO) {
    log.info("delete groupId=$groupId")
    groupGuard.requireWrite(groupId)
    inTransaction {
      val group = groupRepository.findById(groupId) ?: throw NotFoundException("group not found")
      if (repositoryRepository.countByGroupId(groupId) > 0) {
        throw ConflictException("The group still owns repositories. Delete them before deleting the group.")
      }
      val assignments = userGroupAssignmentRepository.findAllByGroupId(groupId)
      if (assignments.any { it.role == RoleInGroup.owner && ownsNoOtherGroup(it.userId, groupId) }) {
        throw ConflictException(
          "The group is the only group one of its owners owns. Deleting it would lock that member out.",
        )
      }
      assignments.forEach { userGroupAssignmentRepository.delete(it) }
      groupRepository.delete(group)
    }
  }

  override suspend fun listAssignments(): List<GroupAssignmentSummary> = withContext(Dispatchers.IO) {
    log.info("listAssignments userId=${coroutineContext.userId()}")
    findAllByUserId(coroutineContext.userId()).map { assignment ->
      GroupAssignmentSummary(
        groupId = assignment.groupId,
        role = assignment.role,
        name = groupRepository.findById(assignment.groupId)?.name ?: "-",
      )
    }
  }

  // pageSize is the true, requested page size: drop uses it as-is (so the offset of every page
  // stays correct), and take asks for one extra row (limit = pageSize + 1) so the caller can
  // answer hasMore from one call. Inflating pageSize itself before calling this — as the old
  // GroupHttpController.listGroupMembers did — shifts the offset too and skips a row at every
  // page boundary (T6 review). findAllByGroupId is ordered (createdAt asc, id asc) so drop/take
  // is deterministic.
  override suspend fun listMembers(groupId: GroupId, page: Int, pageSize: Int): List<UserGroupAssignment> =
    withContext(Dispatchers.IO) {
      log.info("listMembers groupId=$groupId page=$page pageSize=$pageSize")
      findByIdForUser(groupId) ?: throw NotFoundException("group not found")
      val fixedPage = page.coerceAtLeast(0)
      val fixedPageSize = pageSize.coerceAtLeast(0).coerceAtMost(100)
      userGroupAssignmentRepository.findAllByGroupId(groupId)
        .drop(fixedPage * fixedPageSize)
        .take(fixedPageSize + 1)
    }

  override suspend fun addUserToGroup(
    userId: UserId,
    groupId: GroupId,
    role: RoleInGroup,
  ): UserGroupAssignment = withContext(Dispatchers.IO) {
    log.info("add user $userId to group: $groupId")
    val group = groupGuard.requireWrite(groupId)

    val newAssigment = UserGroupAssignment(
      userId = userId,
      groupId = group.id,
      role = role,
    )

    userGroupAssignmentRepository.save(newAssigment)
  }

  /**
   * Removing an owner is refused when it would leave the group without an owner, or the removed user
   * without any group they own — the latter could no longer log in or create a token.
   */
  override suspend fun removeUserFromGroup(groupId: GroupId, userId: UserId) = withContext(Dispatchers.IO) {
    log.info("removeUserFromGroup userId=$userId groupId=$groupId")
    groupGuard.requireWrite(groupId)

    inTransaction {
      val assignment = userGroupAssignmentRepository.findByUserIdAndGroupId(userId, groupId)
        ?: throw IllegalArgumentException("assignment not found")
      if (assignment.role == RoleInGroup.owner) {
        val otherOwnerExists = userGroupAssignmentRepository.findAllByGroupId(groupId)
          .any { it.role == RoleInGroup.owner && it.userId != userId }
        if (!otherOwnerExists) {
          throw ConflictException("This is the group's last owner. Add another owner before removing this one.")
        }
        if (ownsNoOtherGroup(userId, groupId)) {
          throw ConflictException(
            "This is the only group the user owns. Removing them would lock them out.",
          )
        }
      }
      userGroupAssignmentRepository.delete(assignment)
    }
  }

  override suspend fun findAllByUserId(userId: UserId): List<UserGroupAssignment> = withContext(Dispatchers.IO) {
    log.info("findAllByUserId userId=$userId")
    userGroupAssignmentRepository.findAllByUserId(userId)
  }

  /** Whether [userId] holds no owner assignment in any group other than [groupId]. */
  private fun ownsNoOtherGroup(userId: UserId, groupId: GroupId): Boolean =
    userGroupAssignmentRepository.findAllByUserId(userId)
      .none { it.role == RoleInGroup.owner && it.groupId != groupId }

  /** Runs [block] in one database transaction on the calling (IO) thread; any exception rolls it back. */
  private fun inTransaction(block: () -> Unit) {
    transactionTemplate.executeWithoutResult { block() }
  }
}
