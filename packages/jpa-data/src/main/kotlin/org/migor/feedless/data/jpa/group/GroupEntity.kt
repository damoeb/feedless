package org.migor.feedless.data.jpa.group

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.data.jpa.userGroup.UserGroupAssignmentEntity
import java.util.*

@Entity
@Table(
  name = "t_group",
  uniqueConstraints = [
    UniqueConstraint(name = "UniqueGroupName", columnNames = [StandardJpaFields.name, StandardJpaFields.ownerId])]
)
open class GroupEntity : EntityWithUUID() {

  @Column(nullable = false, name = StandardJpaFields.name)
  open lateinit var name: String

  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.ownerId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_group__to__user")
  )
  open var owner: UserEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "userId")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var memberships: MutableList<UserGroupAssignmentEntity> = mutableListOf()
}

fun GroupEntity.toDomain(): org.migor.feedless.group.Group {
  return GroupMapper.Companion.INSTANCE.toDomain(this)
}
