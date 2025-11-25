package org.migor.feedless.group

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.Mother.randomGroupId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.any2
import org.migor.feedless.argThat
import org.migor.feedless.eq
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class GroupServiceTest {

  private val currentUserId = UserId()
  private val userId = randomUserId()
  private val groupId = randomGroupId()
  private lateinit var userGroupAssignmentDAO: UserGroupAssignmentRepository
  private lateinit var userDAO: UserRepository
  private lateinit var groupService: GroupService
  private lateinit var user: User
  private lateinit var currentUser: User
  private lateinit var group: Group

  @BeforeEach
  fun setUp() = runTest {
    userGroupAssignmentDAO = mock(UserGroupAssignmentRepository::class.java)
    userDAO = mock(UserRepository::class.java)
    val groupDAO = mock(GroupRepository::class.java)

    user = mock(User::class.java)
    `when`(user.id).thenReturn(userId)

    currentUser = mock(User::class.java)
    `when`(currentUser.id).thenReturn(currentUserId)
    `when`(userDAO.findById(any2())).thenReturn(currentUser)

    group = mock(Group::class.java)
    `when`(group.id).thenReturn(groupId)

    groupService = GroupService(userGroupAssignmentDAO, userDAO, groupDAO)
    `when`(userGroupAssignmentDAO.save(any2())).thenAnswer { it.arguments[0] }
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
    runTest(context = RequestContext(userId = currentUserId)) {
      // given
      mockCurrentUserIsAdmin(true)

      // when
      groupService.addUserToGroup(user, group, role)

      // then
      verify(userGroupAssignmentDAO).save(argThat { it.userId == userId && it.groupId == groupId && it.role == role })
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
    runTest(context = RequestContext(userId = currentUserId)) {
      // given
      mockCurrentUserIsAdmin(false)
      mockCurrentUserRoleForGroup(RoleInGroup.owner)

      // when
      groupService.addUserToGroup(user, group, role)

      // then
      verify(userGroupAssignmentDAO).save(argThat { it.userId == userId && it.groupId == groupId && it.role == role })
    }

  @Test
  fun `editor of group cannot add a user with role owner`() =
    runTest(context = RequestContext(userId = currentUserId)) {
      // given
      mockCurrentUserIsAdmin(false)
      mockCurrentUserRoleForGroup(RoleInGroup.editor)

      // when/then
      assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
        runBlocking(RequestContext(userId = currentUserId)) {
          groupService.addUserToGroup(user, group, RoleInGroup.owner)
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
    runTest(context = RequestContext(userId = currentUserId)) {
      // given
      mockCurrentUserRoleForGroup(role)

      // when/then
      assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
        runBlocking(RequestContext(userId = currentUserId)) {
          groupService.addUserToGroup(user, group, RoleInGroup.viewer)
        }
      }
    }

  @Test
  fun `admin can remove user from group`() = runTest(context = RequestContext(userId = currentUserId)) {
    // given
    mockCurrentUserIsAdmin(true)
    val assignment = mockUserRoleForGroup(userId, RoleInGroup.owner)

    // when
    groupService.removeUserFromGroup(user, group)

    // then
    verify(userGroupAssignmentDAO).delete(eq(assignment))
  }

  @Test
  fun `owner can remove user from group`() = runTest(context = RequestContext(userId = currentUserId)) {
    // given
    mockCurrentUserIsAdmin(false)
    mockUserRoleForGroup(currentUserId, RoleInGroup.owner)
    val assignment = mockUserRoleForGroup(userId, RoleInGroup.owner)

    // when
    groupService.removeUserFromGroup(user, group)

    // then
    verify(userGroupAssignmentDAO).delete(eq(assignment))
  }


  @ParameterizedTest
  @CsvSource(
    value = [
      "viewer",
      "editor",
    ]
  )
  fun `others cannot remove a user from group`(role: RoleInGroup) =
    runTest(context = RequestContext(userId = currentUserId)) {
      mockCurrentUserRoleForGroup(role)
      assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
        runTest(context = RequestContext(userId = currentUserId)) {
          groupService.removeUserFromGroup(user, group)
        }
      }
    }


  private suspend fun mockCurrentUserRoleForGroup(role: RoleInGroup): UserGroupAssignment {
    return mockUserRoleForGroup(currentUserId, role)
  }

  private suspend fun mockUserRoleForGroup(userId: UserId, role: RoleInGroup): UserGroupAssignment {
    val assignment = mock(UserGroupAssignment::class.java)
    `when`(assignment.role).thenReturn(role)
    `when`(userGroupAssignmentDAO.findByUserIdAndGroupId(eq(userId), any2())).thenReturn(assignment)
    return assignment
  }

  private fun mockCurrentUserIsAdmin(isAdmin: Boolean) {
    `when`(currentUser.admin).thenReturn(isAdmin)
  }


}
