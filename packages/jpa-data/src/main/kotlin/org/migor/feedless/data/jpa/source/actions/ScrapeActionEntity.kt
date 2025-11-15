package org.migor.feedless.data.jpa.source.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.source.SourceEntity
import java.util.*

@Entity(name = "t_scrape_action")
@Inheritance(strategy = InheritanceType.JOINED)
open class ScrapeActionEntity : EntityWithUUID() {

  @Column(name = "pos", nullable = false)
  open var pos: Int? = null

  @Column(name = StandardJpaFields.sourceId, nullable = false)
  open lateinit var sourceId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.sourceId,
    referencedColumnName = StandardJpaFields.id,
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_scrape_action__to__source")
  )
  open var source: SourceEntity? = null
}

