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
@Profile("${AppProfiles.document} & ${AppLayer.api}")
class RecordHttpController(
  private val documentUseCase: DocumentUseCasePort,
  private val documentGuard: DocumentGuardPort,
  private val mapper: HttpRecordMapper,
) : RecordsApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listRecords(
    repositoryId: java.util.UUID,
    page: Int,
    pageSize: Int,
  ): ResponseEntity<RecordListResponse> {
    // Ask for one more than the page holds: a full page is not evidence of a next one.
    val pageable = PageableRequest(pageNumber = page, pageSize = pageSize + 1)
    val fetched = documentUseCase
      .findAllByRepositoryId(
        RepositoryId(repositoryId.toString()),
        pageable = pageable,
      )
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
    val document = requireRecordForRead(repositoryId, recordId)
    return ResponseEntity.ok(mapper.toHttp(document))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun createRecord(
    repositoryId: java.util.UUID,
    recordCreate: RecordCreate,
  ): ResponseEntity<HttpRecord> {
    val created = documentUseCase.createDocument(
      mapper.toDomainCreate(RepositoryId(repositoryId.toString()), recordCreate),
    )
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toHttp(created))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun updateRecord(
    repositoryId: java.util.UUID,
    recordId: java.util.UUID,
    recordUpdate: RecordUpdate,
  ): ResponseEntity<HttpRecord> {
    requireRecordForWrite(repositoryId, recordId)
    val updated = documentUseCase.updateDocument(
      mapper.toDomainUpdate(recordUpdate),
      DocumentId(recordId.toString()),
    )
    return ResponseEntity.ok(mapper.toHttp(updated))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun deleteRecord(
    repositoryId: java.util.UUID,
    recordId: java.util.UUID,
  ): ResponseEntity<Unit> {
    requireRecordForWrite(repositoryId, recordId)
    documentUseCase.deleteDocuments(
      RepositoryId(repositoryId.toString()),
      StringFilter(`in` = listOf(recordId.toString())),
    )
    return ResponseEntity.noContent().build()
  }

  private suspend fun requireRecordForRead(
    repositoryId: java.util.UUID,
    recordId: java.util.UUID,
  ): Document = requireInRepository(repositoryId, recordId, documentGuard.requireRead(DocumentId(recordId.toString())))

  private suspend fun requireRecordForWrite(
    repositoryId: java.util.UUID,
    recordId: java.util.UUID,
  ): Document = requireInRepository(repositoryId, recordId, documentGuard.requireWrite(DocumentId(recordId.toString())))

  private fun requireInRepository(
    repositoryId: java.util.UUID,
    recordId: java.util.UUID,
    document: Document,
  ): Document {
    if (document.repositoryId != RepositoryId(repositoryId.toString())) {
      throw NotFoundException("record $recordId not found in repository $repositoryId")
    }
    return document
  }
}
