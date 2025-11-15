package org.migor.feedless.data.jpa.userGroup

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
interface UserGroupAssignmentDAO : JpaRepository<UserGroupAssignmentEntity, UUID> {

  @Query(
    """SELECT DISTINCT m FROM UserGroupAssignmentEntity m
    LEFT JOIN FETCH m.group
    WHERE m.id = :id"""
  )
  fun findAllByUserId(@Param("id") userId: UUID): List<UserGroupAssignmentEntity>
  fun findByUserIdAndGroupId(userId: UUID, groupId: UUID): UserGroupAssignmentEntity?
}
