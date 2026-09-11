package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ConflictException
import org.migor.feedless.NotFoundException
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestId
import org.migor.feedless.harvest.HarvestUseCasePort
import org.migor.feedless.http.api.HarvestsApi
import org.migor.feedless.http.api.model.HarvestListResponse
import org.migor.feedless.http.api.model.HarvestRequest
import org.migor.feedless.http.mapper.HttpHarvestMapper
import org.migor.feedless.http.mapper.HttpScrapeFlowMapper
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import org.migor.feedless.throttle.Throttled
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import org.migor.feedless.http.api.model.Harvest as HttpHarvest

@RestController
@RequestMapping("/api/v1")
// RepositoryAccessGuard's profiles too: without the guard this controller cannot exist.
@Profile("${AppProfiles.source} & ${AppProfiles.repository} & ${AppProfiles.user} & ${AppLayer.api}")
class HarvestHttpController(
  private val harvestUseCase: HarvestUseCasePort,
  private val accessGuard: RepositoryAccessGuard,
  private val mapper: HttpHarvestMapper,
  private val scrapeFlowMapper: HttpScrapeFlowMapper,
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
    // findAllBySourceId already asks for one extra; inflating pageSize here too would shift every later page.
    val fetched = harvestUseCase.findAllBySourceId(source.id, dryRun, page, pageSize)
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

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun runSource(
    repositoryId: java.util.UUID,
    sourceId: java.util.UUID,
    harvestRequest: HarvestRequest?,
  ): ResponseEntity<HttpHarvest> {
    // A dry run is a write too: it consumes agent capacity, so public-repository readers cannot start one.
    val source = accessGuard.requireSource(RepositoryId(repositoryId), SourceId(sourceId), RepositoryAccess.write)
    val dryRun = harvestRequest?.dryRun == true
    val storedFlow = harvestRequest?.flow?.let { flow ->
      if (!dryRun) {
        throw InvalidFieldException(
          "flow",
          "only allowed with dryRun: true; a real run always uses the saved flow — save a flow with PATCH",
        )
      }
      try {
        scrapeFlowMapper.toStoredFlow(flow)
      } catch (e: IllegalArgumentException) {
        throw InvalidFieldException("flow", e.message ?: "invalid flow")
      }
    }
    if (!dryRun && source.disabled) {
      throw ConflictException("source ${source.id.uuid} is disabled; enable it, or start a dry run")
    }

    val harvest = harvestUseCase.enqueue(source.id, dryRun, storedFlow)
    return ResponseEntity
      .accepted()
      .location(URI.create("/api/v1/repositories/$repositoryId/sources/$sourceId/harvests/${harvest.id.uuid}"))
      .body(mapper.toHttp(harvest))
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
