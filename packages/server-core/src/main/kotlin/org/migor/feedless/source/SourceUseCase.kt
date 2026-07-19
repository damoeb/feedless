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
import org.migor.feedless.NotFoundException
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.data.jpa.source.toDomain
import org.migor.feedless.data.jpa.source.toEntity
import org.migor.feedless.repository.RepositorySourceUpdate
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
) : SourceUseCasePort {

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


  override suspend fun createSources(sources: List<Source>, repositoryId: RepositoryId): List<Source> =
    withContext(Dispatchers.IO) {
      log.info("creating ${sources.size} sources")

      val groupId = coroutineContext.groupId()

      val createSources = mutableListOf<Source>()
      val createScrapeActions = mutableListOf<ScrapeAction>()
      sources.map { source ->
        source.copy(repositoryId = repositoryId)
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
      sourceRepository.findAllWithActionsByIdIn(createSources.map { it.id })
    }

  override suspend fun updateSources(repositoryId: RepositoryId, updateInputs: List<RepositorySourceUpdate>) =
    withContext(Dispatchers.IO) {
      log.info("updating ${updateInputs.size} sources")

      val repository = repositoryRepository.findById(repositoryId)!!
      if (repository.groupId != coroutineContext.groupId()) {
        throw IllegalArgumentException("Cannot update a source with a group id '${repository.groupId}'")
      }

      val modifiedSources = mutableListOf<Source>()
      val deleteScrapeActions = mutableListOf<ScrapeAction>()
      val saveScrapeActions = mutableListOf<ScrapeAction>()

      updateInputs.forEach { sourceUpdate ->
        var source = sourceRepository.findById(sourceUpdate.sourceId)
          ?: throw NotFoundException("Source ${sourceUpdate.sourceId} not found")
        if (source.repositoryId != repositoryId) {
          throw NotFoundException("Source ${sourceUpdate.sourceId} not found")
        }

        var changed = false

        source = sourceUpdate.tags?.let {
          changed = true
          source.copy(tags = it.toTypedArray())
        } ?: source

        source = sourceUpdate.title?.let {
          changed = true
          source.copy(title = it)
        } ?: source

        if (sourceUpdate.clearLatLng) {
          changed = true
          source = source.copy(latLon = null)
        } else {
          source = sourceUpdate.latLng?.let { point ->
            changed = true
            source.copy(latLon = point)
          } ?: source
        }

        source = sourceUpdate.disabled?.let { disabled ->
          changed = true
          source.copy(
            disabled = disabled,
            errorsInSuccession = 0
          )
        } ?: source

        if (sourceUpdate.clearActions || sourceUpdate.actions != null) {
          deleteScrapeActions.addAll(scrapeActionRepository.findAllBySourceId(source.id))
          sourceUpdate.actions?.let { actions ->
            val savedActions = actions.mapIndexed { index, scrapeAction ->
              val actionEntity = scrapeAction.toEntity()
              actionEntity.sourceId = source.id.uuid
              actionEntity.pos = index
              actionEntity.toDomain()
            }
            saveScrapeActions.addAll(savedActions)
          }
        }

        if (changed) {
          modifiedSources.add(source)
        }
      }

      scrapeActionRepository.deleteAll(deleteScrapeActions)
      scrapeActionRepository.saveAll(saveScrapeActions)
      sourceRepository.saveAll(modifiedSources)
      Unit
    }

  override suspend fun deleteAllById(repositoryId: RepositoryId, sourceIds: List<SourceId>) = withContext(Dispatchers.IO) {
    val repository = repositoryRepository.findById(repositoryId)!!
    if (repository.groupId != coroutineContext.groupId()) {
      throw IllegalArgumentException("Cannot delete a source with a group id '${repository.groupId}'")
    }

    log.info("removing ${sourceIds.size} sources")
    val sources = sourceRepository.findAllByRepositoryIdAndIdIn(repositoryId, sourceIds)
    if (sources.isEmpty()) {
      throw NotFoundException("No sources found to delete")
    }
    sourceRepository.deleteAllById(sources.map { it.id })
  }
}
