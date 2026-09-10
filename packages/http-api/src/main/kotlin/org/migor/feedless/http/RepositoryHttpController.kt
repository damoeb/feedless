package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.http.api.RepositoriesApi
import org.migor.feedless.http.api.model.RepositoryCreate
import org.migor.feedless.http.api.model.RepositoryListResponse
import org.migor.feedless.http.api.model.RepositoryUpdate
import org.migor.feedless.http.api.model.Visibility
import org.migor.feedless.http.mapper.HttpRepositoryMapper
import org.migor.feedless.repository.FulltextQueryFilter
import org.migor.feedless.repository.RepositoriesFilter
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCasePort
import org.migor.feedless.repository.VerticalFilter
import org.migor.feedless.repository.VisibilityFilter
import org.migor.feedless.throttle.Throttled
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.migor.feedless.http.api.model.Repository as HttpRepository
import org.migor.feedless.http.api.model.VerticalFilter as VerticalFilterDto
import kotlin.coroutines.coroutineContext

@RestController
@RequestMapping("/api/v1")
// RepositoryAccessGuard's profiles too: without the guard this controller cannot exist.
@Profile("${AppProfiles.repository} & ${AppProfiles.source} & ${AppProfiles.user} & ${AppLayer.api}")
class RepositoryHttpController(
  private val repositoryUseCase: RepositoryUseCasePort,
  private val accessGuard: RepositoryAccessGuard,
  private val mapper: HttpRepositoryMapper,
) : RepositoriesApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listRepositories(
    page: Int,
    pageSize: Int,
    product: VerticalFilterDto?,
    visibility: Visibility?,
    q: String?,
  ): ResponseEntity<RepositoryListResponse> {
    // Ask for one more than the page holds: a full page is not evidence of a next one.
    val pageable = PageableRequest.withExtraForHasMore(page, pageSize)
    val where = toFilter(product, visibility, q)
    val userId = currentUserId()
    val fetched = repositoryUseCase.findAllByUserId(pageable, where, userId)
    val items = fetched.take(pageSize)
      .map { mapper.toHttp(it, userId != null && it.ownerId == userId) }
    return ResponseEntity.ok(
      RepositoryListResponse(
        items = items,
        hasMore = fetched.size > pageSize,
        totalCount = repositoryUseCase.countAllByUserId(where, userId),
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasToken()")
  @Throttled
  override suspend fun createRepository(
    repositoryCreate: RepositoryCreate,
  ): ResponseEntity<HttpRepository> {
    val userId = currentUserId()
    val created = repositoryUseCase.create(listOf(mapper.toDomainCreate(repositoryCreate)))
      .firstOrNull() ?: throw IllegalStateException("repository was not created")
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(mapper.toHttp(created, userId != null && created.ownerId == userId))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getRepository(repositoryId: java.util.UUID): ResponseEntity<HttpRepository> {
    val repo = accessGuard.requireRepository(RepositoryId(repositoryId), RepositoryAccess.read)
    val userId = currentUserId()
    return ResponseEntity.ok(mapper.toHttp(repo, userId != null && repo.ownerId == userId))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun updateRepository(
    repositoryId: java.util.UUID,
    repositoryUpdate: RepositoryUpdate,
  ): ResponseEntity<HttpRepository> {
    val id = RepositoryId(repositoryId)
    accessGuard.requireRepository(id, RepositoryAccess.write)
    repositoryUseCase.updateRepository(id, mapper.toDomainUpdate(repositoryUpdate))
    val updated = accessGuard.requireRepository(id, RepositoryAccess.write)
    val userId = currentUserId()
    return ResponseEntity.ok(mapper.toHttp(updated, userId != null && updated.ownerId == userId))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun deleteRepository(repositoryId: java.util.UUID): ResponseEntity<Unit> {
    val id = RepositoryId(repositoryId)
    // Check first: the use case answers a foreign repository with 403, which confirms it exists.
    accessGuard.requireRepository(id, RepositoryAccess.write)
    repositoryUseCase.delete(id)
    return ResponseEntity.noContent().build()
  }

  private suspend fun currentUserId(): UserId? = coroutineContext[RequestContext]?.userId

  private fun toFilter(
    product: VerticalFilterDto?,
    visibility: Visibility?,
    q: String?,
  ): RepositoriesFilter? {
    if (product == null && visibility == null && q == null) return null
    return RepositoriesFilter(
      product = product?.let { VerticalFilter(eq = mapper.toDomainVertical(it)) },
      visibility = visibility?.let {
        VisibilityFilter(`in` = listOf(mapper.toDomainVisibility(it)))
      },
      text = q?.let { FulltextQueryFilter(query = it) },
    )
  }
}
