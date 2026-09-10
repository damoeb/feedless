package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.http.api.SourcesApi
import org.migor.feedless.http.api.model.SourceCreate
import org.migor.feedless.http.api.model.SourceListResponse
import org.migor.feedless.http.api.model.SourceUpdate
import org.migor.feedless.http.mapper.HttpSourceMapper
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.SourceUseCasePort
import org.migor.feedless.source.SourcesFilter
import org.migor.feedless.throttle.Throttled
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.migor.feedless.http.api.model.Source as HttpSource

@RestController
@RequestMapping("/api/v1")
// RepositoryAccessGuard's profiles too: without the guard this controller cannot exist.
@Profile("${AppProfiles.source} & ${AppProfiles.repository} & ${AppProfiles.user} & ${AppLayer.api}")
class SourceHttpController(
  private val sourceUseCase: SourceUseCasePort,
  private val sourceRepository: SourceRepository,
  private val accessGuard: RepositoryAccessGuard,
  private val mapper: HttpSourceMapper,
) : SourcesApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listSources(
    repositoryId: java.util.UUID,
    page: Int,
    pageSize: Int,
    disabled: Boolean?,
    like: String?,
    minErrorsInSuccession: Int?,
  ): ResponseEntity<SourceListResponse> {
    val repository = accessGuard.requireRepository(RepositoryId(repositoryId), RepositoryAccess.read)
    // Ask for one more than the page holds: a full page is not evidence of a next one.
    val pageable = PageableRequest(pageNumber = page, pageSize = pageSize + 1)
    val where = toFilter(disabled, like, minErrorsInSuccession)
    val fetched = sourceRepository.findAllByRepositoryIdFiltered(repository.id, pageable, where, null)
    val items = fetched.take(pageSize).map { mapper.toHttp(it) }
    return ResponseEntity.ok(
      SourceListResponse(
        items = items,
        hasMore = fetched.size > pageSize,
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listUserSources(
    page: Int,
    pageSize: Int,
    disabled: Boolean?,
    like: String?,
    minErrorsInSuccession: Int?,
  ): ResponseEntity<SourceListResponse> {
    val (userId, groupIds) = accessGuard.requireCallerScope()
    // Ask for one more than the page holds: a full page is not evidence of a next one.
    val pageable = PageableRequest(pageNumber = page, pageSize = pageSize + 1)
    val where = toFilter(disabled, like, minErrorsInSuccession)
    val fetched = sourceRepository.findAllForUser(userId, groupIds, pageable, where)
    val items = fetched.take(pageSize).map { mapper.toHttp(it) }
    return ResponseEntity.ok(
      SourceListResponse(
        items = items,
        hasMore = fetched.size > pageSize,
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasToken()")
  @Throttled
  override suspend fun createSource(
    repositoryId: java.util.UUID,
    sourceCreate: SourceCreate,
  ): ResponseEntity<HttpSource> {
    val repository = accessGuard.requireRepository(RepositoryId(repositoryId), RepositoryAccess.write)
    val created = sourceUseCase.createSources(
      listOf(mapper.toDomainSource(sourceCreate)),
      repository.id,
    ).firstOrNull() ?: throw IllegalStateException("source was not created")
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toHttp(created))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
  ): ResponseEntity<HttpSource> {
    val source = accessGuard.requireSource(RepositoryId(repositoryId), SourceId(sourceId), RepositoryAccess.read)
    return ResponseEntity.ok(mapper.toHttp(source))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun updateSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
    sourceUpdate: SourceUpdate,
  ): ResponseEntity<HttpSource> {
    val repoId = RepositoryId(repositoryId)
    val id = SourceId(sourceId)
    accessGuard.requireSource(repoId, id, RepositoryAccess.write)
    sourceUseCase.updateSources(repoId, listOf(mapper.toDomainUpdate(sourceId, sourceUpdate)))
    return ResponseEntity.ok(mapper.toHttp(accessGuard.requireSource(repoId, id, RepositoryAccess.write)))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun deleteSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
  ): ResponseEntity<Unit> {
    val repoId = RepositoryId(repositoryId)
    val id = SourceId(sourceId)
    accessGuard.requireSource(repoId, id, RepositoryAccess.write)
    sourceUseCase.deleteAllById(repoId, listOf(id))
    return ResponseEntity.noContent().build()
  }

  private fun toFilter(disabled: Boolean?, like: String?, minErrorsInSuccession: Int?): SourcesFilter? =
    if (disabled == null && like == null && minErrorsInSuccession == null) {
      null
    } else {
      SourcesFilter(disabled = disabled, like = like, minErrorsInSuccession = minErrorsInSuccession)
    }
}
