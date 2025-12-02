package org.migor.feedless.document

import org.migor.feedless.PageableRequest
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

interface DocumentRepository {

  suspend fun deleteAllByRepositoryIdAndStatusWithSkip(repositoryId: RepositoryId, status: ReleaseStatus, skip: Int)

  suspend fun deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(
    repositoryId: RepositoryId,
    date: LocalDateTime,
    status: ReleaseStatus
  )

  suspend fun deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(
    id: RepositoryId,
    maxDate: LocalDateTime,
    released: ReleaseStatus
  )

  suspend fun deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
    id: RepositoryId,
    maxDate: LocalDateTime,
    released: ReleaseStatus
  )

  suspend fun findByTitleInAndRepositoryId(titles: List<String>, repositoryId: RepositoryId): Document?

  suspend fun countByRepositoryId(id: RepositoryId): Long
  suspend fun findAllByRepositoryId(id: RepositoryId): List<Document>

  suspend fun findAllByRepositoryIdAndIdIn(repositoryId: RepositoryId, ids: List<DocumentId>): List<Document>
  suspend fun findAllBySourceId(sourceId: SourceId, pageable: PageableRequest): List<Document>


  suspend fun findByIdWithSource(documentId: DocumentId): Document?
  suspend fun countBySourceId(sourceId: SourceId): Int

  suspend fun findFirstByContentHashOrUrlAndRepositoryId(
    contentHash: String,
    url: String,
    repositoryId: RepositoryId
  ): Document?


  suspend fun findAllWithAttachmentsByIdIn(ids: List<DocumentId>): List<Document>
  suspend fun findById(id: DocumentId): Document?
  suspend fun deleteAllById(ids: List<DocumentId>)
  suspend fun save(document: Document): Document
  suspend fun deleteById(id: DocumentId)
  suspend fun saveAll(documents: List<Document>): List<Document>
  suspend fun findAllFiltered(
    repositoryId: RepositoryId,
    filter: DocumentsFilter?,
    orderBy: RecordOrderBy?,
    status: ReleaseStatus,
    tags: List<String>,
    pageable: PageableRequest
  ): List<Document>

  suspend fun getRecordFrequency(filter: DocumentsFilter, groupBy: DocumentDateField): List<DocumentFrequency>

}
