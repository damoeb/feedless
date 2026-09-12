package org.migor.feedless.session

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentId
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.UUID

class ActingGroupTest {

  private val userId = UserId()
  private val repository = mock(UserGroupAssignmentRepository::class.java)
  private val now = LocalDateTime.now()

  @Test
  fun `the acting group is the oldest group the user owns, whatever order the assignments arrive in`() {
    val oldestOwned = GroupId()
    `when`(repository.findAllByUserId(userId)).thenReturn(
      listOf(
        assignment(GroupId(), RoleInGroup.owner, createdAt = now.minusDays(1)),
        assignment(GroupId(), RoleInGroup.viewer, createdAt = now.minusDays(9)),
        assignment(oldestOwned, RoleInGroup.owner, createdAt = now.minusDays(5)),
        assignment(GroupId(), RoleInGroup.owner, createdAt = now.minusDays(3)),
      )
    )

    assertThat(repository.actingGroupOf(userId)).isEqualTo(GroupAndRole(oldestOwned, RoleInGroup.owner))
  }

  @Test
  fun `owned groups created at the same instant are ordered by assignment id`() {
    val firstById = GroupId()
    `when`(repository.findAllByUserId(userId)).thenReturn(
      listOf(
        assignment(GroupId(), RoleInGroup.owner, createdAt = now, id = "ffffffff-0000-0000-0000-000000000000"),
        assignment(firstById, RoleInGroup.owner, createdAt = now, id = "0fffffff-0000-0000-0000-000000000000"),
        assignment(GroupId(), RoleInGroup.owner, createdAt = now, id = "80000000-0000-0000-0000-000000000000"),
      )
    )

    assertThat(repository.actingGroupOf(userId).groupId).isEqualTo(firstById)
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

  @Test
  fun `a user owns a group only while holding an owner assignment in it`() {
    val owned = GroupId()
    val viewed = GroupId()
    `when`(repository.findByUserIdAndGroupId(userId, owned)).thenReturn(assignment(owned, RoleInGroup.owner))
    `when`(repository.findByUserIdAndGroupId(userId, viewed)).thenReturn(assignment(viewed, RoleInGroup.viewer))

    assertThat(repository.ownsGroup(userId, owned)).isTrue()
    assertThat(repository.ownsGroup(userId, viewed)).isFalse()
    assertThat(repository.ownsGroup(userId, GroupId())).isFalse()
  }

  private fun assignment(
    groupId: GroupId,
    role: RoleInGroup,
    createdAt: LocalDateTime = now,
    id: String = UUID.randomUUID().toString(),
  ) = UserGroupAssignment(id = UserGroupAssignmentId(id), userId = userId, groupId = groupId, role = role, createdAt = createdAt)
}
