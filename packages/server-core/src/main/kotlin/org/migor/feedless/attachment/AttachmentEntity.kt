package org.migor.feedless.attachment

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.document.DocumentEntity
import org.springframework.context.annotation.Lazy
import java.util.*

@Entity
@Table(name = "t_attachment")
open class AttachmentEntity : EntityWithUUID() {

  companion object {
    const val LEN_URL = 1000
  }

  @Column(nullable = false, name = "has_data")
  open var hasData: Boolean = false

  @Column(length = LEN_URL, name = "remote_data_url")
  open var remoteDataUrl: String? = null

  @Column(nullable = false, name = "content_type")
  open lateinit var contentType: String

  @Column(length = LEN_URL, name = "original_url")
  open var originalUrl: String? = null

  @Column(name = "name")
  open var name: String? = null

  @Column(name = "size")
  open var size: Long? = null

  @Column(name = "duration")
  open var duration: Long? = null

  @Lazy
  @Column(columnDefinition = "bytea", name = "data")
  open var data: ByteArray? = null

  @Column(name = StandardJpaFields.documentId, nullable = false)
  open lateinit var documentId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.documentId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_attachment__to__document")
  )
  open var document: DocumentEntity? = null

  @PrePersist
  fun prePersist() {
    this.hasData = data != null
    if (hasData && StringUtils.isNotBlank(remoteDataUrl)) {
      throw IllegalArgumentException("Data and remoteDataUrl is present")
    }
    if (!hasData && StringUtils.isBlank(remoteDataUrl)) {
      throw IllegalArgumentException("Neither data nor remoteDataUrl is present")
    }
  }

}

fun createAttachmentUrl(propertyService: PropertyService, id: UUID): String = "${propertyService.apiGatewayUrl}/attachment/${id}"
