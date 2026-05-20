package org.migor.feedless.http

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.http.api.RepositoriesApi
import org.migor.feedless.http.mapper.toGraphql
import org.migor.feedless.http.mapper.toHttpDto
import org.migor.feedless.http.mapper.toPageable
import org.migor.feedless.http.mapper.toRepositoriesFilter
import org.migor.feedless.http.model.CountResponse
import org.migor.feedless.http.model.Repository
import org.migor.feedless.http.model.RepositoryCreate
import org.migor.feedless.http.model.RepositoryListResponse
import org.migor.feedless.http.model.RepositoryUpdate
import org.migor.feedless.http.model.Vertical
import org.migor.feedless.http.model.Visibility
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@RestController
@RequestMapping("/api/v1")
@Profile("${AppProfiles.repository} & ${AppLayer.api}")
class RepositoriesHttpController(
  private val repositoryUseCase: RepositoryUseCase,
  private val repositoryRepository: RepositoryRepository,
  private val sourceRepository: SourceRepository,
  private val httpAuthSupport: HttpAuthSupport,
) : RepositoriesApi {

  private val httpRequest: HttpServletRequest
    get() = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

  @Throttled
  @GetMapping("/repositories")
  override fun listRepositories(
    page: Int,
    pageSize: Int,
    product: Vertical?,
    visibility: Visibility?,
    q: String?,
  ): ResponseEntity<RepositoryListResponse> = runBlocking {
    httpAuthSupport.withUserContext(httpRequest) { ctx ->
      try {
        val userId = ctx.userId!!
        val pageable = toPageable(page, pageSize)
        val items = if (pageable.pageSize == 0) {
          emptyList()
        } else {
          repositoryUseCase.findAllByUserId(pageable, toRepositoriesFilter(product, visibility, q), userId)
        }
        ResponseEntity.ok(
          RepositoryListResponse(
            items = items.map { repo ->
              enrichRepository(repo, userId)
            },
          ),
        )
      } catch (e: Exception) {
        toErrorResponse(e)
      }
    }
  }

  @Throttled
  @GetMapping("/repositories/count")
  override fun countRepositories(product: Vertical): ResponseEntity<CountResponse> = runBlocking {
    httpAuthSupport.withUserContext(httpRequest) { ctx ->
      try {
        val count = repositoryUseCase.countAll(ctx.userId!!, product.toGraphql().fromDto())
        ResponseEntity.ok(CountResponse(count = count))
      } catch (e: Exception) {
        toErrorResponse(e)
      }
    }
  }

  @Throttled
  @GetMapping("/repositories/{repositoryId}")
  override fun getRepository(repositoryId: java.util.UUID): ResponseEntity<Repository> = runBlocking {
    httpAuthSupport.withUserContext(httpRequest) { ctx ->
      try {
        val userId = ctx.userId!!
        val repository = repositoryRepository.findById(RepositoryId(repositoryId))
          ?: return@withUserContext ResponseEntity.notFound().build()
        ResponseEntity.ok(enrichRepository(repository, userId))
      } catch (e: Exception) {
        toErrorResponse(e)
      }
    }
  }

  @Throttled
  @PostMapping("/repositories")
  @PreAuthorize("@capabilityService.hasToken()")
  override fun createRepositories(repositoryCreate: List<RepositoryCreate>): ResponseEntity<List<Repository>> =
    runBlocking {
      httpAuthSupport.withUserContext(httpRequest) { ctx ->
        try {
          val userId = ctx.userId!!
          val created = repositoryUseCase.create(repositoryCreate.map { it.toGraphql() })
          ResponseEntity.status(HttpStatus.CREATED).body(
            created.map { enrichRepository(it, userId) },
          )
        } catch (e: Exception) {
          toErrorResponse(e)
        }
      }
    }

  @Throttled
  @PatchMapping("/repositories/{repositoryId}")
  @PreAuthorize("@capabilityService.hasCapability('user')")
  override fun updateRepository(
    repositoryId: java.util.UUID,
    repositoryUpdate: RepositoryUpdate,
  ): ResponseEntity<Unit> = runBlocking {
    httpAuthSupport.withUserContext(httpRequest) { _ ->
      try {
        repositoryUseCase.updateRepository(RepositoryId(repositoryId), repositoryUpdate.toGraphql())
        ResponseEntity.noContent().build()
      } catch (e: Exception) {
        toErrorResponse(e)
      }
    }
  }

  @Throttled
  @DeleteMapping("/repositories/{repositoryId}")
  @PreAuthorize("@capabilityService.hasCapability('user')")
  override fun deleteRepository(repositoryId: java.util.UUID): ResponseEntity<Unit> = runBlocking {
    httpAuthSupport.withUserContext(httpRequest) { _ ->
      try {
        repositoryUseCase.delete(RepositoryId(repositoryId))
        ResponseEntity.noContent().build()
      } catch (e: Exception) {
        toErrorResponse(e)
      }
    }
  }

  private suspend fun enrichRepository(
    repository: org.migor.feedless.repository.Repository,
    userId: UserId,
  ): Repository {
    val repoId = repository.id
    val sourcesCount = sourceRepository.countByRepositoryId(repoId).toInt()
    val sourcesCountWithProblems = sourceRepository.countSourcesWithProblems(repoId)
    return repository.toHttpDto(userId, sourcesCount, sourcesCountWithProblems)
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T> toErrorResponse(e: Exception): ResponseEntity<T> {
    return when (e) {
      is PermissionDeniedException -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
      is IllegalArgumentException -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
      is NoSuchElementException -> ResponseEntity.notFound().build()
      else -> throw e
    }
  }
}
