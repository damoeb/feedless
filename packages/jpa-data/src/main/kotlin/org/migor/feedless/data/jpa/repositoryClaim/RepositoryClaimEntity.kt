package org.migor.feedless.data.jpa.repositoryClaim

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.repository.RepositoryClaimMapper
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.repository.RepositoryClaim
import java.util.*


@Entity
@Table(
  name = "t_repository_claim",
)
open class RepositoryClaimEntity : EntityWithUUID() {

  @Column(name = StandardJpaFields.repositoryId)
  open var repositoryId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.repositoryId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_repository_claim__to__repository")
  )
  open var repository: RepositoryEntity? = null
}

fun RepositoryClaimEntity.toDomain(): RepositoryClaim {
  return RepositoryClaimMapper.INSTANCE.toDomain(this)
}

fun RepositoryClaim.toEntity(): RepositoryClaimEntity {
  return RepositoryClaimMapper.INSTANCE.toEntity(this)
}
