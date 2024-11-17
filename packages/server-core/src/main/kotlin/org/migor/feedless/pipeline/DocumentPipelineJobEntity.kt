package org.migor.feedless.pipeline

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.type.SqlTypes
import org.jetbrains.annotations.NotNull
import org.migor.feedless.actions.PluginExecutionJsonEntity
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.document.DocumentEntity
import java.util.*

@Entity
@DiscriminatorValue("d")
open class DocumentPipelineJobEntity : PipelineJobEntity() {

  @NotNull
  @Column(name = "executor_id")
  open lateinit var pluginId: String

  @NotNull
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "executor_params", columnDefinition = "jsonb")
  open lateinit var executorParams: PluginExecutionJsonEntity

  @NotNull
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
