package org.migor.feedless.data.jpa.userGroup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
  override suspend fun findAllByUserId(userId: UserId): List<UserGroupAssignment> {
    return withContext(Dispatchers.IO) {
      userGroupAssignmentDAO.findAllByUserId(userId.uuid).map { it.toDomain() }
    }
  }

  override suspend fun findByUserIdAndGroupId(
    userId: UserId,
    groupId: GroupId
  ): UserGroupAssignment? {
    return withContext(Dispatchers.IO) {
      userGroupAssignmentDAO.findByUserIdAndGroupId(userId.uuid, groupId.uuid)?.toDomain()
    }
  }

  override suspend fun save(assignment: UserGroupAssignment): UserGroupAssignment {
    return withContext(Dispatchers.IO) {
      userGroupAssignmentDAO.save(assignment.toEntity()).toDomain()
    }
  }

  override suspend fun delete(assignment: UserGroupAssignment) {
    withContext(Dispatchers.IO) {
      userGroupAssignmentDAO.delete(assignment.toEntity())
    }
  }

}
