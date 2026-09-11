package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.PreconditionFailedException
import org.migor.feedless.http.api.SourcesApi
import org.migor.feedless.http.api.model.GeoPoint
import org.migor.feedless.http.api.model.ScrapeFlow
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
  private val etagCalculator: ETagCalculator,
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
    val pageable = PageableRequest.withExtraForHasMore(page, pageSize)
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
    val pageable = PageableRequest.withExtraForHasMore(page, pageSize)
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
    val httpSource = mapper.toHttp(source)
    return ResponseEntity.ok().eTag(etagCalculator.compute(editableFields(httpSource))).body(httpSource)
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun updateSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
    sourceUpdate: SourceUpdate,
    ifMatch: String?,
  ): ResponseEntity<HttpSource> {
    val repoId = RepositoryId(repositoryId)
    val id = SourceId(sourceId)
    // Access guard first: a denied or missing repository/source answers 404, never 412 — a
    // stale If-Match must never leak that a source exists.
    val current = accessGuard.requireSource(repoId, id, RepositoryAccess.write)
    if (ifMatch != null && ifMatch != "*" && ifMatch != etagCalculator.compute(editableFields(mapper.toHttp(current)))) {
      throw PreconditionFailedException("source ${id.uuid} was modified since the ETag in If-Match")
    }
    // Check-then-update race: two PATCHes with the same (matching) If-Match can both pass this
    // check and both apply — acceptable for slice 1 per the plan; no locking added here.
    sourceUseCase.updateSources(repoId, listOf(mapper.toDomainUpdate(sourceId, sourceUpdate)))
    val updated = mapper.toHttp(accessGuard.requireSource(repoId, id, RepositoryAccess.write))
    return ResponseEntity.ok().eTag(etagCalculator.compute(editableFields(updated))).body(updated)
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

  /**
   * The fields a client can actually change via [SourceUpdate] — title, tags, disabled, latLng,
   * flow. Excludes lastRefreshedAt, lastRecordsRetrieved, errorsInSuccession, and
   * lastErrorMessage: every harvest tick rewrites those, so hashing them made a harvest landing
   * between a CLI GET and PATCH answer a spurious 412 even though nobody edited the source.
   */
  private fun editableFields(source: HttpSource) = SourceEditableFields(
    title = source.title,
    tags = source.tags,
    disabled = source.disabled,
    latLng = source.latLng,
    flow = source.flow,
  )

  private data class SourceEditableFields(
    val title: String,
    val tags: List<String>?,
    val disabled: Boolean?,
    val latLng: GeoPoint?,
    val flow: ScrapeFlow,
  )
}
