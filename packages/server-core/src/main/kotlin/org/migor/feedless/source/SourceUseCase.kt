package org.migor.feedless.source

import jakarta.validation.Validation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.api.mapper.fromDto
import org.migor.feedless.api.mapper.toSource
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.data.jpa.source.toDomain
import org.migor.feedless.data.jpa.source.toEntity
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.SourceUpdateInput
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.group.GroupId
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.pipelineJob.PipelineJobStatus
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.UserId
import org.migor.feedless.user.groupId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
@Profile("${AppProfiles.source} & ${AppLayer.service}")
class SourceUseCase(
  private val sourcePipelineJobRepository: SourcePipelineJobRepository,
  private val sourceRepository: SourceRepository,
  @Lazy private val repositoryHarvester: RepositoryHarvester,
  private val planConstraintsService: PlanConstraintsService,
  private val scrapeActionRepository: ScrapeActionRepository,
  private val repositoryRepository: RepositoryRepository,
  private val sourcePipelineService: SourcePipelineService
) {

  private val log = LoggerFactory.getLogger(SourceUseCase::class.simpleName)

  suspend fun processSourcePipeline(sourceId: SourceId, jobs: List<SourcePipelineJob>) = withContext(Dispatchers.IO) {
    log.info("${jobs.size} processSourcePipeline for source $sourceId")

    val job = jobs.first().copy(
      status = PipelineJobStatus.IN_PROGRESS
    )

    sourcePipelineJobRepository.save(job)
    val source = sourceRepository.findByIdWithActions(sourceId)!!

    val updatedJob = try {
      try {
        repositoryHarvester.scrapeSource(patchRequestUrl(source, job.url), LogCollector())
        log.info("job ${job.id} done")
        job.copy(
          status = PipelineJobStatus.SUCCEEDED
        )
      } catch (e: ResumableHarvestException) {
        log.info("delaying: ${e.message}")
        job.copy(coolDownUntil = LocalDateTime.now().plus(e.nextRetryAfter))
      }
    } catch (e: Exception) {
      log.warn("aborting scrape job, cause ${e.message}")
      job.copy(
        status = PipelineJobStatus.FAILED,
        logs = e.message
      )
    }
    try {
      sourcePipelineJobRepository.save(updatedJob)
    } catch (e: Exception) {
      log.warn("${e.message}]", e)
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

  fun processSourceJobs() {
    try {
      val groupedSources = sourcePipelineJobRepository.findAllPendingBatched(LocalDateTime.now())
        .groupBy { it.sourceId }

//      sourcePipelineJobRepository.incrementAttemptCount(groupedSources.values.flatMap { it.map { it.id } }.distinct())

      if (groupedSources.isNotEmpty()) {
        val semaphore = Semaphore(5)
        runBlocking {
          runCatching {
            coroutineScope {
              groupedSources.map { groupedSources ->
                try {
                  val (userId, groupId) = getOwnerIdsForSourceId(groupedSources.key)
                  async(RequestContext(userId = userId, groupId = groupId)) {
                    semaphore.acquire()
                    delay(300)
                    try {
                      processSourcePipeline(groupedSources.key, groupedSources.value)
                    } catch (t: Throwable) {
                      if (t !is ResumableHarvestException) {
                        log.error("processDocumentPlugins fatal failure ${t.message}")
                        sourceRepository.setErrorState(groupedSources.key, true, t.message)
                      }
                    } finally {
                      semaphore.release()
                    }
                  }
                } catch (e: Exception) {
                  async {}
                }
              }.awaitAll()
            }
            log.info("done")
          }.onFailure {
            log.error("batch refresh done: ${it.message}", it)
          }
        }
      }
    } catch (e: Exception) {
      log.error(e.message, e)
    }
  }

  private suspend fun getOwnerIdsForSourceId(sourceId: SourceId): Pair<UserId, GroupId> = withContext(Dispatchers.IO) {
    val repo =
      withContext(Dispatchers.IO) { repositoryRepository.findBySourceId(sourceId) }
        ?: throw sourcePipelineService.failAfterCleaningJobsForSource(
          sourceId
        )
    Pair(repo.ownerId, repo.groupId)
  }


  suspend fun createSources(sourceInputs: List<SourceInput>, repositoryId: RepositoryId) =
    withContext(Dispatchers.IO) {
      log.info("creating ${sourceInputs.size} sources")

      val groupId = coroutineContext.groupId()

      val createSources = mutableListOf<Source>()
      val createScrapeActions = mutableListOf<ScrapeAction>()
      sourceInputs.map {
        it.toSource().copy(
          repositoryId = repositoryId
        )
      }
        .map { source: Source ->
          planConstraintsService.auditScrapeRequestMaxActions(source.actions.size, groupId)
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

  suspend fun updateSources(repositoryId: RepositoryId, updateInputs: List<SourceUpdateInput>) =
    withContext(Dispatchers.IO) {
      log.info("updating ${updateInputs.size} sources")

      val repository = repositoryRepository.findById(repositoryId)!!
      if (repository.groupId != coroutineContext.groupId()) {
        throw IllegalArgumentException("Cannot update a source with a group id '${repository.groupId}'")
      }

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

  suspend fun deleteAllById(repositoryId: RepositoryId, sourceIds: List<SourceId>) = withContext(Dispatchers.IO) {
    // todo verify permissions
//    val repository = repositoryRepository.findById(repositoryId)!!
//    if (repository.groupId != coroutineContext.groupId()) {
//      throw IllegalArgumentException("Cannot update a source with a group id '${repository.groupId}'")
//    }

    log.info("removing ${sourceIds.size} sources")
    val sources = sourceRepository.findAllByRepositoryIdAndIdIn(repositoryId, sourceIds)
    sourceRepository.deleteAllById(sources.map { it.id })
  }
}
