package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.PageableRequest
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentGuardPort
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentUseCasePort
import org.migor.feedless.document.StringFilter
import org.migor.feedless.http.api.RecordsApi
import org.migor.feedless.http.api.model.RecordCreate
import org.migor.feedless.http.api.model.RecordListResponse
import org.migor.feedless.http.api.model.RecordUpdate
import org.migor.feedless.http.mapper.HttpRecordMapper
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.throttle.Throttled
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.migor.feedless.http.api.model.Record as HttpRecord

@RestController
@RequestMapping("/api/v1")
// RepositoryAccessGuard's profiles too: without the guard this controller cannot exist.
@Profile(
  "${AppProfiles.document} & ${AppProfiles.repository} & ${AppProfiles.source} & ${AppProfiles.user} & ${AppLayer.api}",
)
class RecordHttpController(
  private val documentUseCase: DocumentUseCasePort,
  private val documentGuard: DocumentGuardPort,
  private val accessGuard: RepositoryAccessGuard,
  private val mapper: HttpRecordMapper,
) : RecordsApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listRecords(
    repositoryId: java.util.UUID,
    page: Int,
    pageSize: Int,
  ): ResponseEntity<RecordListResponse> {
    val repository = accessGuard.requireRepository(RepositoryId(repositoryId), RepositoryAccess.read)
    // Ask for one more than the page holds: a full page is not evidence of a next one.
    val pageable = PageableRequest(pageNumber = page, pageSize = pageSize + 1)
    val fetched = documentUseCase.findAllByRepositoryId(repository.id, pageable = pageable)
    val items = fetched.take(pageSize).map { mapper.toHttp(it) }
    return ResponseEntity.ok(
      RecordListResponse(
        items = items,
        hasMore = fetched.size > pageSize,
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getRecord(
    repositoryId: java.util.UUID,
    recordId: java.util.UUID,
  ): ResponseEntity<HttpRecord> {
    val document = requireRecord(RepositoryId(repositoryId), DocumentId(recordId), RepositoryAccess.read)
    return ResponseEntity.ok(mapper.toHttp(document))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun createRecord(
    repositoryId: java.util.UUID,
    recordCreate: RecordCreate,
  ): ResponseEntity<HttpRecord> {
    val repository = accessGuard.requireRepository(RepositoryId(repositoryId), RepositoryAccess.write)
    val created = documentUseCase.createDocument(mapper.toDomainCreate(repository.id, recordCreate))
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toHttp(created))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun updateRecord(
    repositoryId: java.util.UUID,
    recordId: java.util.UUID,
    recordUpdate: RecordUpdate,
  ): ResponseEntity<HttpRecord> {
    val id = DocumentId(recordId)
    requireRecord(RepositoryId(repositoryId), id, RepositoryAccess.write)
    val updated = documentUseCase.updateDocument(mapper.toDomainUpdate(recordUpdate), id)
    return ResponseEntity.ok(mapper.toHttp(updated))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun deleteRecord(
    repositoryId: java.util.UUID,
    recordId: java.util.UUID,
  ): ResponseEntity<Unit> {
    val repoId = RepositoryId(repositoryId)
    requireRecord(repoId, DocumentId(recordId), RepositoryAccess.write)
    documentUseCase.deleteDocuments(repoId, StringFilter(`in` = listOf(recordId.toString())))
    return ResponseEntity.noContent().build()
  }

  /** The repository must pass [accessGuard] first; only then is the record looked up. */
  private suspend fun requireRecord(
    repositoryId: RepositoryId,
    recordId: DocumentId,
    access: RepositoryAccess,
  ): Document {
    accessGuard.requireRepository(repositoryId, access)
    val document = when (access) {
      RepositoryAccess.read -> documentGuard.requireRead(recordId)
      RepositoryAccess.write -> documentGuard.requireWrite(recordId)
    }
    // Same answer for a missing record and one of another repository.
    if (document.repositoryId != repositoryId) {
      throw NotFoundException("record ${recordId.uuid} not found")
    }
    return document
  }
}
