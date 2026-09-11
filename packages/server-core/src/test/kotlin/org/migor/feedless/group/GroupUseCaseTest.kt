package org.migor.feedless.group

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.Mother.randomGroupId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.any2
import org.migor.feedless.argThat
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.eq
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.repository.RepositoryRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

class GroupUseCaseTest {

  private val currentUserId = UserId()
  private val userId = randomUserId()
  private val groupId = randomGroupId()
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository
  private lateinit var groupGuard: GroupGuard
  private lateinit var groupRepository: GroupRepository
  private lateinit var groupUseCase: GroupUseCase
  private lateinit var user: User
  private lateinit var currentUser: User
  private lateinit var group: Group

  @BeforeEach
  fun setUp() = runTest {
    currentUser = mock(User::class.java)
    `when`(currentUser.id).thenReturn(currentUserId)

    val userRepository = mock(UserRepository::class.java)
    `when`(userRepository.findById(currentUserId)).thenReturn(currentUser)

    user = mock(User::class.java)
    `when`(user.id).thenReturn(userId)

    userGroupAssignmentRepository = mock(UserGroupAssignmentRepository::class.java)

    groupRepository = mock(GroupRepository::class.java)
    group = mock(Group::class.java)
    `when`(group.id).thenReturn(groupId)
    `when`(group.ownerId).thenReturn(userId)

    `when`(groupRepository.findById(groupId)).thenReturn(group)

    groupGuard = GroupGuard(
      groupRepository,
      UserGuard(userRepository),
      userGroupAssignmentRepository
    )

    groupUseCase = GroupUseCase(
      userGroupAssignmentRepository,
      groupGuard,
      groupRepository,
      mock(RepositoryRepository::class.java),
      TransactionTemplate(mock(PlatformTransactionManager::class.java)),
    )
    `when`(userGroupAssignmentRepository.save(any2())).thenAnswer { it.arguments[0] }
  }

  @Test
  fun `findByIdForUser returns null when group missing`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      `when`(groupRepository.findById(groupId)).thenReturn(null)

      val result = groupUseCase.findByIdForUser(groupId)

      assertThat(result).isNull()
    }

  @Test
  fun `findByIdForUser throws when user is not member`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      `when`(userGroupAssignmentRepository.findByUserIdAndGroupId(eq(currentUserId), eq(groupId))).thenReturn(null)

      assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
        runBlocking(RequestContext(groupId = GroupId(), userId = currentUserId)) {
          groupUseCase.findByIdForUser(groupId)
        }
      }
    }

  @Test
  fun `delete removes assignments and group`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      mockCurrentUserIsAdmin(true)
      val assignment = mock(UserGroupAssignment::class.java)
      `when`(userGroupAssignmentRepository.findAllByGroupId(groupId)).thenReturn(listOf(assignment))

      groupUseCase.delete(groupId)

      verify(userGroupAssignmentRepository).delete(eq(assignment))
      verify(groupRepository).delete(eq(group))
    }

  @Test
  fun `delete throws NotFoundException when group disappears after guard check`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      mockCurrentUserIsAdmin(true)
      `when`(groupRepository.findById(groupId))
        .thenReturn(group)
        .thenReturn(null)

      assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
        runBlocking(RequestContext(groupId = GroupId(), userId = currentUserId)) {
          groupUseCase.delete(groupId)
        }
      }
    }

  @ParameterizedTest
  @CsvSource(
    value = [
      "owner",
      "viewer",
      "editor",
    ]
  )
  fun `admin can add a user with role`(role: RoleInGroup) =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      // given
      mockCurrentUserIsAdmin(true)

      // when
      groupUseCase.addUserToGroup(user.id, groupId, role)

      // then
      verify(userGroupAssignmentRepository).save(argThat { it.userId == userId && it.groupId == groupId && it.role == role })
    }

  @ParameterizedTest
  @CsvSource(
    value = [
      "owner",
      "viewer",
      "editor",
    ]
  )
  fun `owner of group can add a user with role`(role: RoleInGroup) =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      // given
      mockCurrentUserIsAdmin(false)
      mockCurrentUserRoleForGroup(RoleInGroup.owner)

      // when
      groupUseCase.addUserToGroup(user.id, groupId, role)

      // then
      verify(userGroupAssignmentRepository).save(argThat { it.userId == userId && it.groupId == groupId && it.role == role })
    }

  @Test
  fun `editor of group cannot add a user with role owner`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      // given
      mockCurrentUserIsAdmin(false)
      mockCurrentUserRoleForGroup(RoleInGroup.editor)

      // when/then
      assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
        runBlocking(RequestContext(groupId = GroupId(), userId = currentUserId)) {
          groupUseCase.addUserToGroup(user.id, groupId, RoleInGroup.owner)
        }
      }
    }

  @ParameterizedTest
  @CsvSource(
    value = [
      "viewer",
      "editor",
    ]
  )
  fun `others cannot add a user to group`(role: RoleInGroup) =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      // given
      mockCurrentUserRoleForGroup(role)

      // when/then
      assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
        runBlocking(RequestContext(groupId = GroupId(), userId = currentUserId)) {
          groupUseCase.addUserToGroup(user.id, groupId, RoleInGroup.viewer)
        }
      }
    }

  @Test
  fun `admin can remove user from group`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      // given — removing a non-owner; the last-owner rules are covered by GroupUseCaseIntTest
      mockCurrentUserIsAdmin(true)
      val assignment = mockUserRoleForGroup(userId, RoleInGroup.editor)

      // when
      groupUseCase.removeUserFromGroup(groupId, user.id)

      // then
      verify(userGroupAssignmentRepository).delete(eq(assignment))
    }

  @Test
  fun `owner can remove user from group`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      // given
      mockCurrentUserIsAdmin(false)
      mockUserRoleForGroup(currentUserId, RoleInGroup.owner)
      val assignment = mockUserRoleForGroup(userId, RoleInGroup.editor)

      // when
      groupUseCase.removeUserFromGroup(groupId, user.id)

      // then
      verify(userGroupAssignmentRepository).delete(eq(assignment))
    }


  @ParameterizedTest
  @CsvSource(
    value = [
      "viewer",
      "editor",
    ]
  )
  @Disabled("move to GroupGuard")
  fun `others cannot remove a user from group`(role: RoleInGroup) =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      mockCurrentUserRoleForGroup(role)
      assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
        runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
          groupUseCase.removeUserFromGroup(groupId, user.id)
        }
      }
    }


  @Test
  fun `listMembers returns paged assignments for members, plus one extra row so the caller can answer hasMore`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      mockCurrentUserRoleForGroup(RoleInGroup.viewer)
      val member1 = UserGroupAssignment(userId = UserId(), groupId = groupId, role = RoleInGroup.owner)
      val member2 = UserGroupAssignment(userId = UserId(), groupId = groupId, role = RoleInGroup.viewer)
      val member3 = UserGroupAssignment(userId = UserId(), groupId = groupId, role = RoleInGroup.editor)
      `when`(userGroupAssignmentRepository.findAllByGroupId(groupId)).thenReturn(listOf(member1, member2, member3))

      val page = groupUseCase.listMembers(groupId, page = 1, pageSize = 1)

      // GroupHttpController.listGroupMembers takes the true pageSize (member2) itself and uses
      // the extra row (member3) only to answer hasMore, without a second request.
      assertThat(page).containsExactly(member2, member3)
    }

  @Test
  fun `listMembers pages without skipping or repeating rows, mirroring listGroupMembers' ask-for-one-extra pattern`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      mockCurrentUserRoleForGroup(RoleInGroup.viewer)
      val members = (1..5).map { UserGroupAssignment(userId = UserId(), groupId = groupId, role = RoleInGroup.viewer) }
      `when`(userGroupAssignmentRepository.findAllByGroupId(groupId)).thenReturn(members)

      // Mirrors GroupHttpController.listGroupMembers: ask for one more than the page holds.
      var page = 0
      val returned = mutableListOf<UserGroupAssignment>()
      while (true) {
        val fetched = groupUseCase.listMembers(groupId, page = page, pageSize = 2)
        val hasMore = fetched.size > 2
        returned.addAll(fetched.take(2))
        if (!hasMore) break
        page++
      }

      assertThat(returned).containsExactlyElementsOf(members)
    }

  @Test
  fun `listMembers throws NotFoundException when group missing`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      `when`(groupRepository.findById(groupId)).thenReturn(null)

      assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
        runBlocking(RequestContext(groupId = GroupId(), userId = currentUserId)) {
          groupUseCase.listMembers(groupId, page = 0, pageSize = 20)
        }
      }
    }

  private fun mockCurrentUserRoleForGroup(role: RoleInGroup): UserGroupAssignment {
    return mockUserRoleForGroup(currentUserId, role)
  }

  private fun mockUserRoleForGroup(userId: UserId, role: RoleInGroup): UserGroupAssignment {
    val assignment = mock(UserGroupAssignment::class.java)
    `when`(assignment.role).thenReturn(role)
    `when`(userGroupAssignmentRepository.findByUserIdAndGroupId(eq(userId), any2())).thenReturn(assignment)
    return assignment
  }

  private fun mockCurrentUserIsAdmin(isAdmin: Boolean) {
    `when`(currentUser.admin).thenReturn(isAdmin)
  }

}
