package org.migor.feedless.annotation

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.validation.constraints.Min
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.annotation.AnnotationEntity
import org.migor.feedless.data.jpa.document.DocumentEntity
import java.util.*

@Entity(name = "t_annotation_text")
@DiscriminatorValue("text")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_annotation_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_annotation(id) ON DELETE CASCADE"
  )
)
open class TextAnnotationEntity : AnnotationEntity() {
  @Column(name = "from_char")
  open var fromChar: Int = 0

  @Column(nullable = false, name = "to_char")
  @Min(0)
  open var toChar: Int = 0

  @Column(name = "comment_id", nullable = false)
  open lateinit var commentId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "comment_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_text_annotation__to__document")
  )
  open var comment: DocumentEntity? = null
}
