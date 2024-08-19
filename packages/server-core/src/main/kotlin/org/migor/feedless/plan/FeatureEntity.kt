package org.migor.feedless.plan

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields


@Entity
@Table(
  name = "t_feature", uniqueConstraints = [
    UniqueConstraint(
      name = "UniqueFeatureName",
      columnNames = [StandardJpaFields.name]
    )
  ],
  indexes = [
    Index(name = "name__idx", columnList = StandardJpaFields.name),
  ]
)
open class FeatureEntity : EntityWithUUID() {

  @Column(nullable = false, name = StandardJpaFields.name, length = 50)
  open lateinit var name: String
}
