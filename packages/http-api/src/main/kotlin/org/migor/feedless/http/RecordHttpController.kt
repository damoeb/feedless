package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.document.DocumentGuardPort
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentUseCasePort
import org.migor.feedless.document.StringFilter
import org.migor.feedless.http.api.RecordsApi
import org.migor.feedless.http.api.model.RecordCreate
import org.migor.feedless.http.api.model.RecordDeleteRequest
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
  override suspend fun getRecord(recordId: java.util.UUID): ResponseEntity<HttpRecord> {
    val document = documentGuard.requireRead(DocumentId(recordId.toString()))
    return ResponseEntity.ok(mapper.toHttp(document))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun createRecord(recordCreate: RecordCreate): ResponseEntity<HttpRecord> {
    val created = documentUseCase.createDocument(mapper.toDomainCreate(recordCreate))
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toHttp(created))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun updateRecord(
    recordId: java.util.UUID,
    recordUpdate: RecordUpdate,
  ): ResponseEntity<HttpRecord> {
    val updated = documentUseCase.updateDocument(
      mapper.toDomainUpdate(recordUpdate),
      DocumentId(recordId.toString()),
    )
    return ResponseEntity.ok(mapper.toHttp(updated))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun deleteRecords(
    repositoryId: java.util.UUID,
    recordDeleteRequest: RecordDeleteRequest,
  ): ResponseEntity<Unit> {
    documentUseCase.deleteDocuments(
      RepositoryId(repositoryId.toString()),
      StringFilter(`in` = recordDeleteRequest.ids.map { it.toString() }),
    )
    return ResponseEntity.noContent().build()
  }
}
