package org.migor.feedless.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.harvest.HarvestStatus
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.StoredFlowParser
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs queued harvests. The API and scheduler may be separate processes, so the database is the queue (SKIP LOCKED claims).
 * Every claimed harvest ends COMPLETED: a throw completes it as failed, a dead process via the stale sweep.
 */
@Service
@Profile("${AppProfiles.repository} & ${AppLayer.scheduler}")
class QueuedHarvestExecutor internal constructor(
  private val harvestRepository: HarvestRepository,
  private val sourceRepository: SourceRepository,
  private val repositoryRepository: RepositoryRepository,
  private val repositoryHarvester: RepositoryHarvester,
  private val sourceDryRunner: SourceDryRunner,
  private val storedFlowParser: StoredFlowParser,
) {

  private val log = LoggerFactory.getLogger(QueuedHarvestExecutor::class.simpleName)

  @Scheduled(fixedDelay = 2000, initialDelay = 5000)
  fun executeQueuedHarvests() {
    try {
      completeStaleRuns()
      // Claim no more than can run at once: a claimed harvest waiting for a permit would sit in
      // `running` without running.
      val claimed = try {
        harvestRepository.claimQueued(MAX_CONCURRENT_RUNS, LocalDateTime.now())
      } catch (e: DataIntegrityViolationException) {
        // A real run of that source started between lock and commit, so the database refused the claim; all stay queued.
        log.info("a real harvest of a claimed source started meanwhile, claiming on the next tick")
        emptyList()
      }
      if (claimed.isNotEmpty()) {
        log.info("running ${claimed.size} queued harvests")
        runBlocking { executeAll(claimed) }
      }
    } catch (e: Exception) {
      log.error("queued harvests failed: ${e.message}", e)
    }
  }

  internal suspend fun executeAll(harvests: List<Harvest>) {
    val semaphore = Semaphore(MAX_CONCURRENT_RUNS)
    coroutineScope {
      harvests.map { async { semaphore.withPermit { execute(it) } } }.awaitAll()
    }
  }

  /** Runs one claimed harvest and completes it — as failed if anything goes wrong. */
  internal suspend fun execute(harvest: Harvest): Harvest =
    try {
      val source = sourceRepository.findByIdWithActions(harvest.sourceId)
        ?: throw IllegalStateException("source ${harvest.sourceId.uuid} no longer exists")
      val repository = source.repositoryId?.let { repositoryRepository.findById(it) }
        ?: throw IllegalStateException("repository of source ${source.id.uuid} not found")
      withContext(RequestContext(userId = repository.ownerId, groupId = repository.groupId)) {
        when {
          harvest.dryRun -> sourceDryRunner.dryRun(source.withFlowOf(harvest), harvest)
          // Checked when queued too (409); the source may have been disabled since.
          source.disabled -> completeAsFailed(harvest, "source is disabled; enable it to run it")
          else -> repositoryHarvester.harvestSource(source, harvest)
        }
      }
    } catch (e: Throwable) {
      log.error("harvest ${harvest.id.uuid} failed: ${e.message}", e)
      val failed = runCatching { completeAsFailed(harvest, "harvest failed: ${e.message}") }
        // The stale sweep completes it once it has been running for too long.
        .onFailure { log.error("cannot complete harvest ${harvest.id.uuid}: ${it.message}", it) }
        .getOrDefault(harvest)
      if (e is CancellationException) {
        throw e
      }
      failed
    }

  /** A dry run tests the harvest's override flow when it has one, else the saved flow. */
  private fun Source.withFlowOf(harvest: Harvest): Source =
    harvest.flow?.let { copy(actions = storedFlowParser.storedFlowToDomainActions(it)) } ?: this

  private fun completeAsFailed(harvest: Harvest, message: String): Harvest {
    val now = LocalDateTime.now()
    val failed = harvest.copy(
      status = HarvestStatus.COMPLETED,
      errornous = true,
      finishedAt = now,
      logs = appendHarvestLog(harvest.logs, harvestLogLine(now, message)),
    )
    harvestRepository.save(failed)
    return failed
  }

  private fun completeStaleRuns() {
    val now = LocalDateTime.now()
    val completed = harvestRepository.completeStaleRunning(
      startedBefore = now.minus(STALE_AFTER),
      now = now,
      message = harvestLogLine(
        now,
        "harvest timed out: still running after ${STALE_AFTER.toMinutes()} minutes, so its run was lost (e.g. the scheduler process stopped)",
      ),
    )
    if (completed > 0) {
      log.warn("completed $completed stale harvests as failed")
    }
  }

  internal companion object {
    const val MAX_CONCURRENT_RUNS = 5
    val STALE_AFTER: Duration = Duration.ofMinutes(30)
  }
}
