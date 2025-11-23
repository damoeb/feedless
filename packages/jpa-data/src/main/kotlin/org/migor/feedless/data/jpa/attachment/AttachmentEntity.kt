package org.migor.feedless.data.jpa.attachment

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.validation.constraints.Size
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.attachment.Attachment
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.document.DocumentEntity
import org.migor.feedless.data.jpa.document.DocumentEntity.Companion.LEN_STR_DEFAULT
import org.migor.feedless.data.jpa.document.DocumentEntity.Companion.LEN_URL
import org.springframework.context.annotation.Lazy
import java.util.*

@Entity
@Table(name = "t_attachment")
open class AttachmentEntity : EntityWithUUID() {

    @Column(nullable = false, name = "has_data")
    open var hasData: Boolean = false

    @Size(message = "remoteDataUrl", max = LEN_URL)
    @Column(length = LEN_URL, name = "remote_data_url")
    open var remoteDataUrl: String? = null

    @Size(message = "mimeType", max = LEN_STR_DEFAULT)
    @Column(nullable = false, length = LEN_STR_DEFAULT, name = "content_type")
    open lateinit var mimeType: String

    @Size(message = "originalUrl", max = LEN_URL)
    @Column(length = LEN_URL, name = "original_url")
    open var originalUrl: String? = null

    @Size(message = "name", max = LEN_STR_DEFAULT)
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

fun AttachmentEntity.toDomain(): Attachment {
    return AttachmentMapper.Companion.INSTANCE.toDomain(this)
}

fun Attachment.toEntity(): AttachmentEntity {
    return AttachmentMapper.Companion.INSTANCE.toEntity(this)
}

