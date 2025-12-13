package org.migor.feedless.data.jpa.userGroup

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
class UserGroupAssignmentJpaRepository(private val userGroupAssignmentDAO: UserGroupAssignmentDAO) :
  UserGroupAssignmentRepository {
  override fun findAllByUserId(userId: UserId): List<UserGroupAssignment> {
    return userGroupAssignmentDAO.findAllByUserId(userId.uuid).map { it.toDomain() }
  }

  override fun findByUserIdAndGroupId(
    userId: UserId,
    groupId: GroupId
  ): UserGroupAssignment? {
    return userGroupAssignmentDAO.findByUserIdAndGroupId(userId.uuid, groupId.uuid)?.toDomain()
  }

  override fun save(assignment: UserGroupAssignment): UserGroupAssignment {
    return userGroupAssignmentDAO.save(assignment.toEntity()).toDomain()
  }

  override fun delete(assignment: UserGroupAssignment) {
    userGroupAssignmentDAO.deleteById(assignment.id.uuid)
  }

}
