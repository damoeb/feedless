package org.migor.feedless.group

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields

@Entity
@Table(
  name = "t_group",
  uniqueConstraints = [
    UniqueConstraint(name = "UniqueGroupName", columnNames = [StandardJpaFields.name])]
)
open class GroupEntity : EntityWithUUID() {

  @Column(nullable = false, name = StandardJpaFields.name)
  open lateinit var name: String

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "userId")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var memberships: MutableList<UserGroupAssignmentEntity> = mutableListOf()
}
