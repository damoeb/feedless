package org.migor.feedless.group

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.repository.any
import org.migor.feedless.repository.any2
import org.migor.feedless.repository.argThat
import org.migor.feedless.repository.eq
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.*

class GroupServiceTest {

  private val currentUserId = UUID.randomUUID()
  private val userId = UUID.randomUUID()
  private val groupId = UUID.randomUUID()
  private lateinit var userGroupAssignmentDAO: UserGroupAssignmentDAO
  private lateinit var userDAO: UserDAO
  private lateinit var groupService: GroupService
  private lateinit var user: UserEntity
  private lateinit var currentUser: UserEntity
  private lateinit var group: GroupEntity

  @BeforeEach
  fun setUp() {
    userGroupAssignmentDAO = mock(UserGroupAssignmentDAO::class.java)
    userDAO = mock(UserDAO::class.java)

    user = mock(UserEntity::class.java)
    `when`(user.id).thenReturn(userId)

    currentUser = mock(UserEntity::class.java)
    `when`(currentUser.id).thenReturn(currentUserId)
    `when`(userDAO.findById(any2())).thenReturn(Optional.of(currentUser))

    group = mock(GroupEntity::class.java)
    `when`(group.id).thenReturn(groupId)

    groupService = GroupService(userGroupAssignmentDAO, userDAO)
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
  fun `admin can add a user with role`(role: RoleInGroup) = runTest(context = RequestContext(userId = currentUserId)) {
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
  fun `editor of group cannot add a user with role owner`() {
    // given
    mockCurrentUserIsAdmin(false)
    mockCurrentUserRoleForGroup(RoleInGroup.editor)

    // when
    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
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
      mockCurrentUserRoleForGroup(role)
      assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
        runTest(context = RequestContext(userId = currentUserId)) {
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


  private fun mockCurrentUserRoleForGroup(role: RoleInGroup): UserGroupAssignmentEntity {
    return mockUserRoleForGroup(currentUserId, role)
  }

  private fun mockUserRoleForGroup(userId: UUID, role: RoleInGroup): UserGroupAssignmentEntity {
    val assignment = mock(UserGroupAssignmentEntity::class.java)
    `when`(assignment.role).thenReturn(role)
    `when`(userGroupAssignmentDAO.findByUserIdAndGroupId(eq(userId), any2())).thenReturn(assignment)
    return assignment
  }

  private fun mockCurrentUserIsAdmin(isAdmin: Boolean) {
    `when`(currentUser.admin).thenReturn(isAdmin)
  }


}
