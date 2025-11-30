package org.migor.feedless.data.jpa.repository

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import org.migor.feedless.repository.Repository

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("repository")
open class RepositoryEntity : AbstractRepositoryEntity()

fun RepositoryEntity.toDomain(): Repository {
  return RepositoryMapper.INSTANCE.toDomain(this)
}

fun Repository.toEntity(): RepositoryEntity {
  return RepositoryMapper.INSTANCE.toEntity(this)
}
