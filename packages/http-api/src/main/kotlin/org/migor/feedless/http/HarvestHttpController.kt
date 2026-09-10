package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestId
import org.migor.feedless.harvest.HarvestUseCasePort
import org.migor.feedless.http.api.HarvestsApi
import org.migor.feedless.http.api.model.HarvestListResponse
import org.migor.feedless.http.mapper.HttpHarvestMapper
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.migor.feedless.http.api.model.Harvest as HttpHarvest

@RestController
@RequestMapping("/api/v1")
// RepositoryAccessGuard's profiles too: without the guard this controller cannot exist.
@Profile("${AppProfiles.source} & ${AppProfiles.repository} & ${AppProfiles.user} & ${AppLayer.api}")
class HarvestHttpController(
  private val harvestUseCase: HarvestUseCasePort,
  private val accessGuard: RepositoryAccessGuard,
  private val mapper: HttpHarvestMapper,
) : HarvestsApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listHarvests(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
    page: Int,
    pageSize: Int,
    dryRun: Boolean,
  ): ResponseEntity<HarvestListResponse> {
    val source = accessGuard.requireSource(RepositoryId(repositoryId), SourceId(sourceId), RepositoryAccess.read)
    // Ask for one more than the page holds: a full page is not evidence of a next one.
    val fetched = harvestUseCase.findAllBySourceId(source.id, dryRun, page, pageSize + 1)
    val items = fetched.take(pageSize).map { mapper.toHttp(it) }
    return ResponseEntity.ok(
      HarvestListResponse(
        items = items,
        hasMore = fetched.size > pageSize,
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getHarvest(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
    harvestId: java.util.UUID,
  ): ResponseEntity<HttpHarvest> {
    val harvest = requireHarvest(RepositoryId(repositoryId), SourceId(sourceId), HarvestId(harvestId))
    return ResponseEntity.ok(mapper.toHttp(harvest))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getHarvestLogs(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
    harvestId: java.util.UUID,
  ): ResponseEntity<String> {
    val harvest = requireHarvest(RepositoryId(repositoryId), SourceId(sourceId), HarvestId(harvestId))
    return ResponseEntity.ok(harvest.logs)
  }

  /** The source must pass [accessGuard] first; only then is the harvest looked up. */
  private suspend fun requireHarvest(
    repositoryId: RepositoryId,
    sourceId: SourceId,
    harvestId: HarvestId,
  ): Harvest {
    val source = accessGuard.requireSource(repositoryId, sourceId, RepositoryAccess.read)
    val harvest = harvestUseCase.findById(harvestId)
    // A missing harvest and one of another source must answer identically.
    if (harvest == null || harvest.sourceId != source.id) {
      throw harvestNotFound(harvestId)
    }
    return harvest
  }

  private fun harvestNotFound(harvestId: HarvestId) = NotFoundException("harvest ${harvestId.uuid} not found")
}
