package org.migor.feedless.attachment

import org.migor.feedless.document.DocumentId
import java.time.LocalDateTime

data class Attachment(
    val id: AttachmentId = AttachmentId(),
    val hasData: Boolean = false,
    val data: ByteArray,
    val remoteDataUrl: String? = null,
    val mimeType: String,
    val originalUrl: String? = null,
    val name: String? = null,
    val size: Long? = null,
    val duration: Long? = null,
    val documentId: DocumentId,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Attachment

        if (hasData != other.hasData) return false
        if (size != other.size) return false
        if (duration != other.duration) return false
        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false
        if (remoteDataUrl != other.remoteDataUrl) return false
        if (mimeType != other.mimeType) return false
        if (originalUrl != other.originalUrl) return false
        if (name != other.name) return false
        if (documentId != other.documentId) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hasData.hashCode()
        result = 31 * result + (size?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + id.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + (remoteDataUrl?.hashCode() ?: 0)
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + (originalUrl?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + documentId.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}
