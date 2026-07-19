package org.migor.feedless.http

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
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
    val pageable = PageableRequest(pageNumber = page, pageSize = pageSize)
    val where = if (disabled == null && like == null) {
      null
    } else {
      SourcesFilter(disabled = disabled, like = like)
    }
    val items = sourceRepository
      .findAllByRepositoryIdFiltered(RepositoryId(repositoryId.toString()), pageable, where, null)
      .map { mapper.toHttp(it) }
    return ResponseEntity.ok(
      SourceListResponse(
        items = items,
        hasMore = items.size == pageSize,
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasToken()")
  @Throttled
  override fun createSources(
    repositoryId: java.util.UUID,
    sourceCreate: Flow<SourceCreate>,
  ): ResponseEntity<Flow<HttpSource>> =
    ResponseEntity.status(HttpStatus.CREATED).body(
      flow {
        val creates = sourceCreate.toList()
        val created = sourceUseCase.createSources(
          creates.map { mapper.toDomainSource(it) },
          RepositoryId(repositoryId.toString()),
        )
        created.forEach { source ->
          emit(mapper.toHttp(source))
        }
      }.withHttpApiRequestContext(),
    )

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
  ): ResponseEntity<HttpSource> {
    val source = sourceRepository.findByIdWithActions(SourceId(sourceId.toString()))
      ?: return ResponseEntity.notFound().build()
    if (source.repositoryId != RepositoryId(repositoryId.toString())) {
      return ResponseEntity.notFound().build()
    }
    return ResponseEntity.ok(mapper.toHttp(source))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun updateSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
    sourceUpdate: SourceUpdate,
  ): ResponseEntity<Unit> {
    if (findSourceInRepository(repositoryId, sourceId) == null) {
      return ResponseEntity.notFound().build()
    }
    sourceUseCase.updateSources(
      RepositoryId(repositoryId.toString()),
      listOf(mapper.toDomainUpdate(sourceId, sourceUpdate)),
    )
    return ResponseEntity.noContent().build()
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun deleteSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
  ): ResponseEntity<Unit> {
    if (findSourceInRepository(repositoryId, sourceId) == null) {
      return ResponseEntity.notFound().build()
    }
    sourceUseCase.deleteAllById(
      RepositoryId(repositoryId.toString()),
      listOf(SourceId(sourceId.toString())),
    )
    return ResponseEntity.noContent().build()
  }

  private suspend fun findSourceInRepository(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
  ): Source? {
    val source = sourceRepository.findByIdWithActions(SourceId(sourceId.toString())) ?: return null
    if (source.repositoryId != RepositoryId(repositoryId.toString())) {
      return null
    }
    return source
  }
}
