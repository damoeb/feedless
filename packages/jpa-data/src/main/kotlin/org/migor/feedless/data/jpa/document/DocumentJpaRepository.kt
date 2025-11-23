package org.migor.feedless.data.jpa.document

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.data.jpa.repository.toPageRequest
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.document} & ${AppLayer.repository}")
class DocumentJpaRepository(private val documentDAO: DocumentDAO) : DocumentRepository {

    override suspend fun deleteAllByRepositoryIdAndStatusWithSkip(
        repositoryId: RepositoryId,
        status: ReleaseStatus,
        skip: Int
    ) {
        return withContext(Dispatchers.IO) {
            documentDAO.deleteAllByRepositoryIdAndStatusWithSkip(repositoryId.uuid, status, skip)
        }
    }

    override suspend fun deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(
        repositoryId: RepositoryId,
        date: LocalDateTime,
        status: ReleaseStatus
    ) {
        withContext(Dispatchers.IO) {
            documentDAO.deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(repositoryId.uuid, date, status)
        }
    }

    override suspend fun deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(
        id: RepositoryId,
        maxDate: LocalDateTime,
        released: ReleaseStatus
    ) {
        withContext(Dispatchers.IO) {
            documentDAO.deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(id.uuid, maxDate, released)
        }
    }

    override suspend fun deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
        id: RepositoryId,
        maxDate: LocalDateTime,
        released: ReleaseStatus
    ) {
        withContext(Dispatchers.IO) {
            documentDAO.deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(id.uuid, maxDate, released)
        }
    }

    override suspend fun findByTitleInAndRepositoryId(
        titles: List<String>,
        repositoryId: RepositoryId
    ): Document? {
        return withContext(Dispatchers.IO) {
            documentDAO.findByTitleInAndRepositoryId(titles, repositoryId.uuid)?.toDomain()
        }
    }

    override suspend fun countByRepositoryId(id: RepositoryId): Long {
        return withContext(Dispatchers.IO) {
            documentDAO.countByRepositoryId(id.uuid)
        }
    }

    override suspend fun findAllByRepositoryId(id: RepositoryId): List<Document> {
        return withContext(Dispatchers.IO) {
            documentDAO.findAllByRepositoryId(id.uuid).map { it.toDomain() }
        }
    }

    override suspend fun findAllByRepositoryIdAndIdIn(
        repositoryId: RepositoryId,
        ids: List<DocumentId>
    ): List<Document> {
        return withContext(Dispatchers.IO) {
            documentDAO.findAllByRepositoryIdAndIdIn(repositoryId.uuid, ids.map { it.uuid }).map { it.toDomain() }
        }
    }

    override suspend fun findAllBySourceId(
        sourceId: SourceId,
        pageable: PageableRequest
    ): List<Document> {
        return withContext(Dispatchers.IO) {
            documentDAO.findAllBySourceId(sourceId.uuid, pageable.toPageRequest()).map { it.toDomain() }
        }
    }

    override suspend fun findByIdWithSource(documentId: DocumentId): Document? {
        return withContext(Dispatchers.IO) {
            documentDAO.findByIdWithSource(documentId.uuid)?.toDomain()
        }
    }

    override suspend fun countBySourceId(sourceId: SourceId): Int {
        return withContext(Dispatchers.IO) {
            documentDAO.countBySourceId(sourceId.uuid)
        }
    }

    override suspend fun findFirstByContentHashOrUrlAndRepositoryId(
        contentHash: String,
        url: String,
        repositoryId: RepositoryId
    ): Document? {
        return withContext(Dispatchers.IO) {
            documentDAO.findFirstByContentHashOrUrlAndRepositoryId(contentHash, url, repositoryId.uuid)?.toDomain()
        }
    }

    override suspend fun findAllWithAttachmentsByIdIn(ids: List<DocumentId>): List<Document> {
        return withContext(Dispatchers.IO) {
            documentDAO.findAllWithAttachmentsByIdIn(ids.map { it.uuid }).map { it.toDomain() }
        }
    }

    override suspend fun findById(id: DocumentId): Document? {
        return withContext(Dispatchers.IO) {
            documentDAO.findById(id.uuid).getOrNull()?.toDomain()
        }
    }

    override suspend fun deleteAllById(ids: List<DocumentId>) {
        withContext(Dispatchers.IO) {
            documentDAO.deleteAllById(ids.map { it.uuid })
        }
    }

    override suspend fun save(document: Document): Document {
        return withContext(Dispatchers.IO) {
            documentDAO.save(document.toEntity()).toDomain()
        }
    }

    override suspend fun deleteById(id: DocumentId) {
        withContext(Dispatchers.IO) {
            documentDAO.deleteById(id.uuid)
        }
    }

    override suspend fun saveAll(documents: List<Document>): List<Document> {
        return withContext(Dispatchers.IO) {
            documentDAO.saveAll(documents.map { it.toEntity() }).map { it.toDomain() }
        }
    }
}
