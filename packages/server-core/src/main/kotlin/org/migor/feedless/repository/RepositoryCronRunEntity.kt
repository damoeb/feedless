package org.migor.feedless.repository

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import java.util.*

@Entity
@Table(name = "t_repository_cron_run")
open class RepositoryCronRunEntity : EntityWithUUID() {

  @Column(name = "successful", nullable = false)
  open var isSuccessful: Boolean = false

  @Lob
  @Column(name = "message", nullable = false)
  open lateinit var message: String

  @Column(name = "executed_at", nullable = false)
  open lateinit var executedAt: Date

  @Column(name = StandardJpaFields.repositoryId, nullable = false)
  open lateinit var repositoryId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.repositoryId,
    referencedColumnName = StandardJpaFields.id,
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_repository_run__to__repository")
  )
  open var repository: RepositoryEntity? = null
}
