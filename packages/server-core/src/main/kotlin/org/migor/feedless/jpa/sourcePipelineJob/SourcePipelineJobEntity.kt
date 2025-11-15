package org.migor.feedless.jpa.sourcePipelineJob

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
import org.jetbrains.annotations.NotNull
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.jpa.document.DocumentEntity.Companion.LEN_URL
import org.migor.feedless.jpa.source.SourceEntity
import org.migor.feedless.pipeline.PipelineJobEntity
import java.util.*

@Entity
@DiscriminatorValue("s")
open class SourcePipelineJobEntity : PipelineJobEntity() {

  @NotNull
  @Column(name = "url", length = LEN_URL)
  open lateinit var url: String

  @NotNull
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
