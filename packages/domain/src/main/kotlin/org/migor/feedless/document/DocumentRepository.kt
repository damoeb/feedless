package org.migor.feedless.document

import org.migor.feedless.PageableRequest
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

interface DocumentRepository {

  fun deleteAllByRepositoryIdAndStatusWithSkip(repositoryId: RepositoryId, status: ReleaseStatus, skip: Int)

  fun deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(
    repositoryId: RepositoryId,
    date: LocalDateTime,
    status: ReleaseStatus
  )

  fun deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(
    id: RepositoryId,
    maxDate: LocalDateTime,
    released: ReleaseStatus
  )

  fun deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
    id: RepositoryId,
    maxDate: LocalDateTime,
    released: ReleaseStatus
  )

  fun findByTitleInAndRepositoryId(titles: List<String>, repositoryId: RepositoryId): Document?

  fun countByRepositoryId(id: RepositoryId): Long
  fun findAllByRepositoryId(id: RepositoryId): List<Document>

  fun findAllByRepositoryIdAndIdIn(repositoryId: RepositoryId, ids: List<DocumentId>): List<Document>
  fun findAllBySourceId(sourceId: SourceId, pageable: PageableRequest): List<Document>


  fun findByIdWithSource(documentId: DocumentId): Document?
  fun countBySourceId(sourceId: SourceId): Int

  fun findFirstByContentHashOrUrlAndRepositoryId(
    contentHash: String,
    url: String,
    repositoryId: RepositoryId
  ): Document?


  fun findAllWithAttachmentsByIdIn(ids: List<DocumentId>): List<Document>
  fun findById(id: DocumentId): Document?
  fun deleteAllById(ids: List<DocumentId>)
  fun save(document: Document): Document
  fun deleteById(id: DocumentId)
  fun saveAll(documents: List<Document>): List<Document>
  fun findAllFiltered(
    repositoryId: RepositoryId,
    filter: DocumentsFilter?,
    orderBy: RecordOrderBy?,
    status: ReleaseStatus,
    tags: List<String>,
    pageable: PageableRequest
  ): List<Document>

  fun getRecordFrequency(filter: DocumentsFilter, groupBy: DocumentDateField): List<DocumentFrequency>

}
