package org.migor.feedless.source

import jakarta.validation.Validation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.api.mapper.fromDto
import org.migor.feedless.api.mapper.toSource
import org.migor.feedless.data.jpa.source.toDomain
import org.migor.feedless.data.jpa.source.toEntity
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.SourceUpdateInput
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.pipelineJob.PipelineJobStatus
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.UserId
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.coroutines.coroutineContext


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.source} & ${AppLayer.service}")
class SourceService(
  private val sourcePipelineJobRepository: SourcePipelineJobRepository,
  private val sourceRepository: SourceRepository,
  @Lazy private val repositoryHarvester: RepositoryHarvester,
  private val documentRepository: DocumentRepository,
  private val planConstraintsService: PlanConstraintsService,
  private val scrapeActionRepository: ScrapeActionRepository
) {

  private val log = LoggerFactory.getLogger(SourceService::class.simpleName)

  fun trackedRepositories() {
//    sourceDAO.getRepositoryHealthByRepositoryIdIn()
  }

  @Transactional
  suspend fun processSourcePipeline(sourceId: SourceId, jobs: List<SourcePipelineJob>) {
    val corrId = coroutineContext.corrId()
    log.info("[$corrId] ${jobs.size} processSourcePipeline for source $sourceId")

    val job = jobs.first().copy(
      status = PipelineJobStatus.IN_PROGRESS
    )


    sourcePipelineJobRepository.save(job)
    val source = sourceRepository.findByIdWithActions(sourceId)!!

    val updatedJob = try {
      try {
        repositoryHarvester.scrapeSource(patchRequestUrl(source, job.url), LogCollector())
        log.info("[$corrId] job ${job.id} done")
        job.copy(
          status = PipelineJobStatus.SUCCEEDED
        )
      } catch (e: ResumableHarvestException) {
        log.info("[$corrId] delaying: ${e.message}")
        job.copy(coolDownUntil = LocalDateTime.now().plus(e.nextRetryAfter))
      }
    } catch (e: Exception) {
      log.warn("[$corrId] aborting scrape job, cause ${e.message}")
      job.copy(
        status = PipelineJobStatus.FAILED,
        logs = e.message
      )
    }
    try {
      sourcePipelineJobRepository.save(updatedJob)
    } catch (e: Exception) {
      log.warn("[$corrId] ${e.message}]", e)
    }
  }

  private fun patchRequestUrl(source: Source, url: String): Source {
    val newSource = source.copy(
      actions = source.actions.map {
        when (it) {
          is FetchAction -> it.copy(
            url = url,
          )

          else -> it
        }
      }
    )
    return newSource
  }

  @Transactional(readOnly = true)
  suspend fun countProblematicSourcesByRepositoryId(repositoryId: RepositoryId): Int {

//      sourceDAO.countByRepositoryIdAndDisabledTrue(repositoryId)
    return sourceRepository.countByRepositoryIdAndLastRecordsRetrieved(repositoryId, 0)

  }

  @Transactional(readOnly = true)
  suspend fun countDocumentsBySourceId(sourceId: SourceId): Int {
    return documentRepository.countBySourceId(sourceId)
  }

  @Transactional
  suspend fun setErrorState(sourceId: SourceId, erroneous: Boolean, message: String?) {
    sourceRepository.setErrorState(sourceId, erroneous, message)
  }

  @Transactional
  suspend fun save(source: Source): Source {
    return sourceRepository.save(source)
  }

  @Transactional(readOnly = true)
  suspend fun countAllByRepositoryId(id: RepositoryId): Long {
    return withContext(Dispatchers.IO) {
      sourceRepository.countByRepositoryId(id)
    }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByRepositoryIdFiltered(
    repositoryId: RepositoryId,
    pageable: PageableRequest,
    where: SourcesFilter? = null,
    orders: List<SourceOrderBy>? = null
  ): List<Source> {
    return sourceRepository.findAllByRepositoryIdFiltered(repositoryId, pageable, where, orders)
  }

  @Transactional
  suspend fun createSources(ownerId: UserId, sourceInputs: List<SourceInput>, repositoryId: RepositoryId) {
    log.info("[${coroutineContext.corrId()}] creating ${sourceInputs.size} sources")

    val createSources = mutableListOf<Source>()
    val createScrapeActions = mutableListOf<ScrapeAction>()
    sourceInputs.map {
      it.toSource().copy(
        repositoryId = repositoryId
      )
    }
      .map { source: Source ->
        planConstraintsService.auditScrapeRequestMaxActions(source.actions.size, ownerId)
//        planConstraintsService.auditScrapeRequestTimeout(scrapeRequest.page.timeout, ownerId)

        if (source.actions.isEmpty()) {
          throw IllegalArgumentException("flow must not be empty")
        }
        val validator = Validation.buildDefaultValidatorFactory().validator
        val invalidActions = source.actions.filter { validator.validate(it).isNotEmpty() }
        if (invalidActions.isNotEmpty()) {
          throw IllegalArgumentException("invalid actions $invalidActions")
        }

        if (validator.validate(source).isNotEmpty()) {
          throw IllegalArgumentException("invalid source")
        }

        val actions = source.actions.mapIndexed { index, scrapeAction ->
          val actionEntity = scrapeAction.toEntity()
          actionEntity.sourceId = source.id.uuid
          actionEntity.pos = index
          actionEntity.toDomain()
        }

        createScrapeActions.addAll(actions)
        createSources.add(
          source.copy(
            actions = emptyList()
          )
        )
      }

    sourceRepository.saveAll(createSources)
    scrapeActionRepository.saveAll(createScrapeActions)

  }

  @Transactional
  suspend fun updateSources(repositoryId: RepositoryId, updateInputs: List<SourceUpdateInput>) {
    log.info("[${coroutineContext.corrId()}] updating ${updateInputs.size} sources")

    // todo check owner

    val modifiedSources = mutableListOf<Source>()
    val deleteScrapeActions = mutableListOf<ScrapeAction>()
    val saveScrapeActions = mutableListOf<ScrapeAction>()

    updateInputs.map { sourceUpdate ->
      var source = sourceRepository.findById(SourceId(sourceUpdate.where.id))!!
      if (source.repositoryId != repositoryId) {
        throw IllegalArgumentException("source does not belong to repository")
      }

      var changed = false

      source = sourceUpdate.data.tags?.let {
        changed = true
        source.copy(tags = it.set.toTypedArray())
      } ?: source

      source = sourceUpdate.data.title?.let {
        changed = true
        source.copy(title = it.set)
      } ?: source

      source = sourceUpdate.data.latLng?.let { point ->
        changed = true

        point.set?.let {
          source.copy(latLon = LatLonPoint(it.lat, it.lng))
        } ?: source.copy(latLon = null)

      } ?: source

      source = sourceUpdate.data.disabled?.let { disabled ->
        changed = true
        source.copy(
          disabled = disabled.set,
          errorsInSuccession = 0
        )
      } ?: source

      sourceUpdate.data.flow?.let { flow ->
        // remove old actions
        deleteScrapeActions.addAll(scrapeActionRepository.findAllBySourceId(source.id))

        flow.set?.let {
          // append new actions
          val actions = flow.set?.fromDto()?.mapIndexed { index, scrapeAction ->
            val actionEntity = scrapeAction.toEntity()
            actionEntity.sourceId = source.id.uuid
            actionEntity.pos = index
            actionEntity.toDomain()
          }
          actions?.let {
            saveScrapeActions.addAll(actions)
          }
        }
      }

//      source.actions = mutableListOf() todo fix this

      if (changed) {
        modifiedSources.add(source)
      }
    }

    scrapeActionRepository.deleteAll(deleteScrapeActions)
    scrapeActionRepository.saveAll(saveScrapeActions)
    sourceRepository.saveAll(modifiedSources)
  }

  @Transactional
  suspend fun deleteAllById(repositoryId: RepositoryId, sourceIds: List<SourceId>) {
    log.info("[${coroutineContext.corrId()}] removing ${sourceIds.size} sources")
    withContext(Dispatchers.IO) {
      val sources = sourceRepository.findAllByRepositoryIdAndIdIn(repositoryId, sourceIds)
      sourceRepository.deleteAllById(sources.map { it.id })
    }
  }

  @Transactional(readOnly = true)
  suspend fun findById(sourceId: SourceId): Source? {
    return withContext(Dispatchers.IO) {
      sourceRepository.findByIdWithActions(sourceId)
    }
  }

}
