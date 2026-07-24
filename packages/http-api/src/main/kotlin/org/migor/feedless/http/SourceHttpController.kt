package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.PageableRequest
import org.migor.feedless.http.api.SourcesApi
import org.migor.feedless.http.api.model.SourceCreate
import org.migor.feedless.http.api.model.SourceListResponse
import org.migor.feedless.http.api.model.SourceUpdate
import org.migor.feedless.http.mapper.HttpSourceMapper
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.SourceUseCasePort
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
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
@Profile("${AppProfiles.source} & ${AppLayer.api}")
class SourceHttpController(
  private val sourceUseCase: SourceUseCasePort,
  private val sourceRepository: SourceRepository,
  private val mapper: HttpSourceMapper,
) : SourcesApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listSources(
    repositoryId: java.util.UUID,
    page: Int,
    pageSize: Int,
    disabled: Boolean?,
    like: String?,
  ): ResponseEntity<SourceListResponse> {
    // Ask for one more than the page holds: a full page is not evidence of a next one.
    val pageable = PageableRequest(pageNumber = page, pageSize = pageSize + 1)
    val where = if (disabled == null && like == null) {
      null
    } else {
      SourcesFilter(disabled = disabled, like = like)
    }
    val fetched = sourceRepository
      .findAllByRepositoryIdFiltered(RepositoryId(repositoryId.toString()), pageable, where, null)
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
    val created = sourceUseCase.createSources(
      listOf(mapper.toDomainSource(sourceCreate)),
      RepositoryId(repositoryId.toString()),
    ).firstOrNull() ?: throw IllegalStateException("source was not created")
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toHttp(created))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
  ): ResponseEntity<HttpSource> =
    ResponseEntity.ok(mapper.toHttp(requireSourceInRepository(repositoryId, sourceId)))

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun updateSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
    sourceUpdate: SourceUpdate,
  ): ResponseEntity<HttpSource> {
    requireSourceInRepository(repositoryId, sourceId)
    sourceUseCase.updateSources(
      RepositoryId(repositoryId.toString()),
      listOf(mapper.toDomainUpdate(sourceId, sourceUpdate)),
    )
    return ResponseEntity.ok(mapper.toHttp(requireSourceInRepository(repositoryId, sourceId)))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun deleteSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
  ): ResponseEntity<Unit> {
    requireSourceInRepository(repositoryId, sourceId)
    sourceUseCase.deleteAllById(
      RepositoryId(repositoryId.toString()),
      listOf(SourceId(sourceId.toString())),
    )
    return ResponseEntity.noContent().build()
  }

  private suspend fun requireSourceInRepository(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
  ): Source {
    val source = sourceRepository.findByIdWithActions(SourceId(sourceId.toString()))
      ?: throw NotFoundException("source $sourceId not found")
    if (source.repositoryId != RepositoryId(repositoryId.toString())) {
      throw NotFoundException("source $sourceId not found in repository $repositoryId")
    }
    return source
  }
}
