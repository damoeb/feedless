package org.migor.feedless.data.jpa.harvest

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
import org.migor.feedless.data.jpa.source.SourceEntity
import java.sql.Types
import java.time.LocalDateTime
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
  open lateinit var startedAt: LocalDateTime

  @Column(name = "finished_at")
  open var finishedAt: LocalDateTime? = null

  @Column(name = StandardJpaFields.sourceId, nullable = false)
  open lateinit var sourceId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.sourceId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_harvest__to__source")
  )
  open var source: SourceEntity? = null
}

