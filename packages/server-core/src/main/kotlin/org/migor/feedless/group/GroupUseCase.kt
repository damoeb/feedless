package org.migor.feedless.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.user.UserId
import org.migor.feedless.user.userId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class GroupUseCase(
  private val userGroupAssignmentRepository: UserGroupAssignmentRepository,
  private val groupGuard: GroupGuard,
  private val groupRepository: GroupRepository,
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

  override suspend fun delete(groupId: GroupId) = withContext(Dispatchers.IO) {
    log.info("delete groupId=$groupId")
    groupGuard.requireWrite(groupId)
    val group = groupRepository.findById(groupId) ?: throw NotFoundException("group not found")
    userGroupAssignmentRepository.findAllByGroupId(groupId).forEach {
      userGroupAssignmentRepository.delete(it)
    }
    groupRepository.delete(group)
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

  override suspend fun removeUserFromGroup(groupId: GroupId, userId: UserId) = withContext(Dispatchers.IO) {
    log.info("removeUserFromGroup userId=$userId groupId=$groupId")
    groupGuard.requireWrite(groupId)

    val assigment = userGroupAssignmentRepository.findByUserIdAndGroupId(userId, groupId)
      ?: throw IllegalArgumentException("assignment not found")
    userGroupAssignmentRepository.delete(assigment)
  }

  override suspend fun findAllByUserId(userId: UserId): List<UserGroupAssignment> = withContext(Dispatchers.IO) {
    log.info("findAllByUserId userId=$userId")
    userGroupAssignmentRepository.findAllByUserId(userId)
  }
}
