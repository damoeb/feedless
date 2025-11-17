package org.migor.feedless.data.jpa.repository

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("repository")
open class RepositoryEntity : AbstractRepositoryEntity()

fun RepositoryEntity.toDomain(): org.migor.feedless.repository.Repository {
  return RepositoryMapper.Companion.INSTANCE.toDomain(this)
}
