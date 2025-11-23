package org.migor.feedless.document

import org.locationtech.jts.geom.Point
import org.migor.feedless.attachment.Attachment
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class Document(
    val id: DocumentId = DocumentId(),
    val url: String,
    val title: String? = null,
    val contentHash: String,
    val raw: ByteArray? = null,
    val rawMimeType: String? = null,
    val latLon: Point? = null,
    val tags: Array<String>? = null,
    val text: String,
    val html: String? = null,
    val imageUrl: String? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val publishedAt: LocalDateTime = LocalDateTime.now(),
    val startingAt: LocalDateTime? = null,
    val isDead: Boolean = false,
    val isFlagged: Boolean = false,
    val score: Int = 0,
    val scoredAt: LocalDateTime? = null,
    val repositoryId: RepositoryId,
    val status: ReleaseStatus,
    val parentId: DocumentId? = null,
    val sourceId: SourceId? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val attachments: List<Attachment> = emptyList(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Document

        if (id != other.id) return false
        if (url != other.url) return false
        if (!tags.contentEquals(other.tags)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + (tags?.contentHashCode() ?: 0)
        return result
    }
}

