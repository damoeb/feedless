package org.migor.feedless.repository

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.Harvest
import java.sql.Types
import java.util.*

@Entity
@Table(
  name = "t_harvest"
)
open class HarvestEntity : EntityWithUUID() {

  @Column(nullable = false, name = "errornous")
  open var errornous: Boolean = false

  @Column(nullable = false, name = "items_added")
  open var itemsAdded: Int = 0

  @Column(nullable = false, name = "items_ignored")
  open var itemsIgnored: Int = 0

  @JdbcTypeCode(Types.LONGVARCHAR)
  @Column(nullable = false, name = "logs")
  open var logs: String = ""

  @Column(name = "started_at")
  open lateinit var startedAt: Date

  @Column(name = "finished_at")
  open var finishedAt: Date? = null

  @Column(name = StandardJpaFields.repositoryId, nullable = false)
  open lateinit var repositoryId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.repositoryId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_harvest__to__repository")
  )
  open var repository: RepositoryEntity? = null
}


fun HarvestEntity.toDto(): Harvest {
  return Harvest(
    errornous = errornous,
    itemsAdded = itemsAdded,
    itemsIgnored = itemsIgnored,
    logs = logs,
    startedAt = startedAt.time,
    finishedAt = finishedAt?.time
  )
}
