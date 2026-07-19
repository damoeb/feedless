package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.harvest.HarvestUseCasePort
import org.migor.feedless.http.api.HarvestsApi
import org.migor.feedless.http.api.model.HarvestListResponse
import org.migor.feedless.http.mapper.HttpHarvestMapper
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Profile("${AppProfiles.source} & ${AppLayer.api}")
class HarvestHttpController(
  private val harvestUseCase: HarvestUseCasePort,
  private val sourceRepository: SourceRepository,
  private val mapper: HttpHarvestMapper,
) : HarvestsApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listHarvests(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
    page: Int,
    pageSize: Int,
    includeLogs: Boolean,
  ): ResponseEntity<HarvestListResponse> {
    val source = sourceRepository.findByIdWithActions(SourceId(sourceId.toString()))
      ?: return ResponseEntity.notFound().build()
    if (source.repositoryId != RepositoryId(repositoryId.toString())) {
      return ResponseEntity.notFound().build()
    }
    val items = harvestUseCase
      .findAllBySourceId(SourceId(sourceId.toString()), page, pageSize)
      .map { mapper.toHttp(it, includeLogs) }
    return ResponseEntity.ok(
      HarvestListResponse(
        items = items,
        hasMore = items.size == pageSize,
      ),
    )
  }
}
