package org.migor.feedless.pipeline

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.models.SourceEntity
import java.util.*

@Entity
@DiscriminatorValue("s")
open class SourcePipelineJobEntity : PipelineJobEntity() {

  @Column(name = "url", length = 500)
  open lateinit var url: String

  @Column(name = StandardJpaFields.sourceId, nullable = true)
  open lateinit var sourceId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.sourceId,
    referencedColumnName = StandardJpaFields.id,
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_pipeline_job__to__source")
  )
  open var source: SourceEntity? = null

  @PrePersist
  fun prePersist() {
    updateStatus()
  }
}
