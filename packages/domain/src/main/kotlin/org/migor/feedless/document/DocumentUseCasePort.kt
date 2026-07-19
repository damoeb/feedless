package org.migor.feedless.document

import org.migor.feedless.PageableRequest
import org.migor.feedless.repository.RepositoryId

interface DocumentUseCasePort {
  suspend fun findById(id: DocumentId): Document?

  suspend fun findAllByRepositoryId(
    repositoryId: RepositoryId,
    filter: DocumentsFilter? = null,
    orderBy: RecordOrderBy? = null,
    status: ReleaseStatus = ReleaseStatus.released,
    tags: List<String> = emptyList(),
    pageable: PageableRequest,
  ): List<Document>

  suspend fun createDocument(data: DocumentCreate): Document

  suspend fun updateDocument(data: DocumentUpdate, id: DocumentId): Document

  suspend fun deleteDocuments(repositoryId: RepositoryId, documentIds: StringFilter)
}
