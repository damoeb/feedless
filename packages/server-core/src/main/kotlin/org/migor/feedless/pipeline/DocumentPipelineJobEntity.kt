package org.migor.feedless.pipeline

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
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
import org.hibernate.annotations.Type
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import java.util.*

@Entity
@DiscriminatorValue("d")
open class DocumentPipelineJobEntity : PipelineJobEntity() {

  @Column(name = "executor_id")
  open lateinit var executorId: String

  @Type(JsonBinaryType::class)
  @Column(name = "executor_params", columnDefinition = "jsonb")
  open lateinit var executorParams: PluginExecutionParamsInput

  @Column(name = StandardJpaFields.documentId, nullable = true)
  open lateinit var documentId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.documentId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_pipeline_job__to__document")
  )
  open var document: DocumentEntity? = null

  @PrePersist
  fun prePersist() {
    updateStatus()
  }
}
