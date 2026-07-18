package org.migor.feedless.http

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.Vertical
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.http.api.RepositoriesApi
import org.migor.feedless.http.api.model.CountResponse
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
import org.migor.feedless.http.api.model.Vertical as HttpVertical
import kotlin.coroutines.coroutineContext

@RestController
@RequestMapping("/api/v1")
@Profile("${AppProfiles.repository} & ${AppLayer.api}")
class RepositoryHttpController(
  private val repositoryUseCase: RepositoryUseCasePort,
  private val mapper: HttpRepositoryMapper,
) : RepositoriesApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listRepositories(
    page: Int,
    pageSize: Int,
    product: HttpVertical?,
    visibility: Visibility?,
    q: String?,
  ): ResponseEntity<RepositoryListResponse> {
    val pageable = PageableRequest(pageNumber = page, pageSize = pageSize)
    val where = toFilter(product, visibility, q)
    val userId = currentUserId()
    val items = repositoryUseCase.findAllByUserId(pageable, where, userId)
      .map { mapper.toHttp(it, userId != null && it.ownerId == userId) }
    return ResponseEntity.ok(
      RepositoryListResponse(
        items = items,
        hasMore = items.size == pageSize,
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasToken()")
  @Throttled
  override fun createRepositories(
    repositoryCreate: Flow<RepositoryCreate>,
  ): ResponseEntity<Flow<HttpRepository>> =
    ResponseEntity.status(HttpStatus.CREATED).body(
      flow {
        val creates = repositoryCreate.toList()
        val userId = coroutineContext[RequestContext]?.userId
        val created = repositoryUseCase.create(mapper.toDomainCreates(creates))
        created.forEach { repo ->
          emit(mapper.toHttp(repo, userId != null && repo.ownerId == userId))
        }
      },
    )

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getRepository(repositoryId: java.util.UUID): ResponseEntity<HttpRepository> {
    val repo = repositoryUseCase.findById(RepositoryId(repositoryId.toString()))
      ?: return ResponseEntity.notFound().build()
    val userId = currentUserId()
    return ResponseEntity.ok(mapper.toHttp(repo, userId != null && repo.ownerId == userId))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun updateRepository(
    repositoryId: java.util.UUID,
    repositoryUpdate: RepositoryUpdate,
  ): ResponseEntity<Unit> {
    repositoryUseCase.updateRepository(
      RepositoryId(repositoryId.toString()),
      mapper.toDomainUpdate(repositoryUpdate),
    )
    return ResponseEntity.noContent().build()
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun deleteRepository(repositoryId: java.util.UUID): ResponseEntity<Unit> {
    repositoryUseCase.delete(RepositoryId(repositoryId.toString()))
    return ResponseEntity.noContent().build()
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun countRepositories(product: HttpVertical): ResponseEntity<CountResponse> {
    val count = repositoryUseCase.countAll(
      currentUserId(),
      Vertical.valueOf(product.name),
    )
    return ResponseEntity.ok(CountResponse(count = count))
  }

  private suspend fun currentUserId(): UserId? = coroutineContext[RequestContext]?.userId

  private fun toFilter(
    product: HttpVertical?,
    visibility: Visibility?,
    q: String?,
  ): RepositoriesFilter? {
    if (product == null && visibility == null && q == null) return null
    return RepositoriesFilter(
      product = product?.let { VerticalFilter(eq = Vertical.valueOf(it.name)) },
      visibility = visibility?.let {
        VisibilityFilter(`in` = listOf(EntityVisibility.valueOf(it.name)))
      },
      text = q?.let { FulltextQueryFilter(query = it) },
    )
  }
}
