package org.migor.feedless.data.jpa.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import java.util.*

@Entity
@Table(
  name = "t_attachment", indexes = [
    Index(name = "idx_attachment_url", columnList = "url")
  ]
)
open class AttachmentEntity : EntityWithUUID() {

  @Column(nullable = false)
  open lateinit var url: String

  @Column(nullable = false)
  open lateinit var type: String

  @Column(nullable = false)
  open var remoteData: Boolean = false

  @Column(columnDefinition = "TEXT")
  open var data: String? = null

  @Column(name = "webDocumentId", nullable = false)
  open lateinit var webDocumentId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "webDocumentId",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_attachment__web_document")
  )
  open var webDocument: WebDocumentEntity? = null

  @PrePersist
  fun prePersist() {
    this.remoteData = data != null
  }

}
