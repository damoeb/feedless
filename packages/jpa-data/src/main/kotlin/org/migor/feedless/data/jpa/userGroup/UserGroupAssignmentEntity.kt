package org.migor.feedless.data.jpa.userGroup

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.group.GroupEntity
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import java.util.*

@Entity
@Table(
  name = "t_user_group_assignment",
  uniqueConstraints = [
    UniqueConstraint(
      name = "UniqueGroupMembership",
      columnNames = [StandardJpaFields.userId, StandardJpaFields.groupId]
    )]
)
open class UserGroupAssignmentEntity : EntityWithUUID() {

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  open lateinit var role: RoleInGroup

  @Column(name = StandardJpaFields.userId, nullable = false)
  open lateinit var userId: UUID

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.userId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_user_group_assignment__to__user")
  )
  open var user: UserEntity? = null

  @Column(name = StandardJpaFields.groupId, nullable = false)
  open lateinit var groupId: UUID

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.groupId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_user_group_assignment__to__group")
  )
  open var group: GroupEntity? = null
}

fun UserGroupAssignmentEntity.toDomain(): UserGroupAssignment {
  return UserGroupAssignmentMapper.Companion.INSTANCE.toDomain(this)
}

fun UserGroupAssignment.toEntity(): UserGroupAssignmentEntity {
  return UserGroupAssignmentMapper.Companion.INSTANCE.toEntity(this)
}
