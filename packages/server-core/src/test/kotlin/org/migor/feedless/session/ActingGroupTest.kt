package org.migor.feedless.session

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ActingGroupTest {

  private val userId = UserId()
  private val repository = mock(UserGroupAssignmentRepository::class.java)

  @Test
  fun `the acting group is the first group the user owns`() {
    val firstOwned = GroupId()
    `when`(repository.findAllByUserId(userId)).thenReturn(
      listOf(
        assignment(GroupId(), RoleInGroup.viewer),
        assignment(GroupId(), RoleInGroup.editor),
        assignment(firstOwned, RoleInGroup.owner),
        assignment(GroupId(), RoleInGroup.owner),
      )
    )

    assertThat(repository.actingGroupOf(userId)).isEqualTo(GroupAndRole(firstOwned, RoleInGroup.owner))
  }

  @Test
  fun `a user who only views or edits groups has no acting group`() {
    `when`(repository.findAllByUserId(userId)).thenReturn(
      listOf(assignment(GroupId(), RoleInGroup.viewer), assignment(GroupId(), RoleInGroup.editor))
    )

    assertThatExceptionOfType(NoActingGroupException::class.java)
      .isThrownBy { repository.actingGroupOf(userId) }
      .withMessageContaining(userId.uuid.toString())
  }

  @Test
  fun `a user without any group has no acting group`() {
    `when`(repository.findAllByUserId(userId)).thenReturn(emptyList())

    assertThatExceptionOfType(NoActingGroupException::class.java).isThrownBy { repository.actingGroupOf(userId) }
  }

  private fun assignment(groupId: GroupId, role: RoleInGroup) =
    UserGroupAssignment(userId = userId, groupId = groupId, role = role)
}
