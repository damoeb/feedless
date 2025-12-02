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
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.eq
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.util.JsonSerializer.toJson
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class GroupServiceTest {

  private lateinit var capabilityService: CapabilityService
  private val currentUserId = UserId()
  private val userId = randomUserId()
  private val groupId = randomGroupId()
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository
  private lateinit var userRepository: UserRepository
  private lateinit var groupService: GroupService
  private lateinit var user: User
  private lateinit var currentUser: User
  private lateinit var group: Group

  @BeforeEach
  fun setUp() = runTest {
    userGroupAssignmentRepository = mock(UserGroupAssignmentRepository::class.java)
    userRepository = mock(UserRepository::class.java)
    val groupRepository = mock(GroupRepository::class.java)

    user = mock(User::class.java)
    `when`(user.id).thenReturn(userId)

    currentUser = mock(User::class.java)
    `when`(currentUser.id).thenReturn(currentUserId)
    `when`(userRepository.findById(any2())).thenReturn(currentUser)

    group = mock(Group::class.java)
    `when`(group.id).thenReturn(groupId)

    capabilityService = mock(CapabilityService::class.java)
    `when`(capabilityService.getCapability(UserCapability.ID))
      .thenReturn(UnresolvedCapability(UserCapability.ID, toJson(UserId())))

    groupService = GroupService(
      userGroupAssignmentRepository,
      userRepository,
      groupRepository,
      capabilityService
    )
    `when`(userGroupAssignmentRepository.save(any2())).thenAnswer { it.arguments[0] }
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
    runTest {
      // given
      mockCurrentUser(currentUserId)
      mockCurrentUserIsAdmin(true)

      // when
      groupService.addUserToGroup(user, group, role)

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
    runTest {
      // given
      mockCurrentUser(currentUserId)
      mockCurrentUserIsAdmin(false)
      mockCurrentUserRoleForGroup(RoleInGroup.owner)

      // when
      groupService.addUserToGroup(user, group, role)

      // then
      verify(userGroupAssignmentRepository).save(argThat { it.userId == userId && it.groupId == groupId && it.role == role })
    }

  @Test
  fun `editor of group cannot add a user with role owner`() =
    runTest {
      // given
      mockCurrentUser(currentUserId)
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
    verify(userGroupAssignmentRepository).delete(eq(assignment))
  }

  @Test
  fun `owner can remove user from group`() = runTest {
    // given
    mockCurrentUser(currentUserId)
    mockCurrentUserIsAdmin(false)
    mockUserRoleForGroup(currentUserId, RoleInGroup.owner)
    val assignment = mockUserRoleForGroup(userId, RoleInGroup.owner)

    // when
    groupService.removeUserFromGroup(user, group)

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
  fun `others cannot remove a user from group`(role: RoleInGroup) =
    runTest {
      mockCurrentUser(currentUserId)
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
    `when`(userGroupAssignmentRepository.findByUserIdAndGroupId(eq(userId), any2())).thenReturn(assignment)
    return assignment
  }

  private fun mockCurrentUserIsAdmin(isAdmin: Boolean) {
    `when`(currentUser.admin).thenReturn(isAdmin)
  }

  private fun mockCurrentUser(currentUserId: UserId) {
    `when`(capabilityService.getCapability(UserCapability.ID))
      .thenReturn(UnresolvedCapability(UserCapability.ID, toJson(currentUserId)))
  }

}
