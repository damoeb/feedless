package org.migor.feedless.http

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.http.api.SourcesApi
import org.migor.feedless.http.mapper.toHttpDto
import org.migor.feedless.http.mapper.toPageable
import org.migor.feedless.http.mapper.toSourcesFilter
import org.migor.feedless.http.model.Source
import org.migor.feedless.http.model.SourceListResponse
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@RestController
@RequestMapping("/api/v1")
@Profile("${AppProfiles.repository} & ${AppLayer.api}")
class SourcesHttpController(
  private val repositoryRepository: RepositoryRepository,
  private val sourceRepository: SourceRepository,
  private val documentRepository: DocumentRepository,
  private val httpAuthSupport: HttpAuthSupport,
) : SourcesApi {

  private val httpRequest: HttpServletRequest
    get() = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

  @Throttled
  @GetMapping("/repositories/{repositoryId}/sources")
  override fun listSources(
    repositoryId: java.util.UUID,
    page: Int,
    pageSize: Int,
    disabled: Boolean?,
    like: String?,
  ): ResponseEntity<SourceListResponse> = runBlocking {
    httpAuthSupport.withUserContext(httpRequest) { _ ->
      try {
        val repoId = RepositoryId(repositoryId)
        if (repositoryRepository.findById(repoId) == null) {
          return@withUserContext ResponseEntity.notFound().build()
        }
        val pageable = toPageable(page, pageSize)
        if (pageable.pageSize == 0) {
          return@withUserContext ResponseEntity.ok(SourceListResponse(items = emptyList()))
        }
        val sources = sourceRepository.findAllByRepositoryIdFiltered(
          repoId,
          pageable,
          toSourcesFilter(disabled, like),
          orders = null,
        )
        ResponseEntity.ok(
          SourceListResponse(
            items = sources.map { source ->
              val recordCount = documentRepository.countBySourceId(source.id)
              source.toHttpDto(recordCount)
            },
          ),
        )
      } catch (e: Exception) {
        toErrorResponse(e)
      }
    }
  }

  @Throttled
  @GetMapping("/repositories/{repositoryId}/sources/{sourceId}")
  override fun getSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
  ): ResponseEntity<Source> = runBlocking {
    httpAuthSupport.withUserContext(httpRequest) { _ ->
      try {
        val repoId = RepositoryId(repositoryId)
        if (repositoryRepository.findById(repoId) == null) {
          return@withUserContext ResponseEntity.notFound().build()
        }
        val source = sourceRepository.findById(SourceId(sourceId))
          ?: return@withUserContext ResponseEntity.notFound().build()
        if (source.repositoryId != repoId) {
          return@withUserContext ResponseEntity.notFound().build()
        }
        val recordCount = documentRepository.countBySourceId(source.id)
        ResponseEntity.ok(source.toHttpDto(recordCount))
      } catch (e: Exception) {
        toErrorResponse(e)
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T> toErrorResponse(e: Exception): ResponseEntity<T> {
    return when (e) {
      is PermissionDeniedException -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
      is IllegalArgumentException -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
      else -> throw e
    }
  }
}
