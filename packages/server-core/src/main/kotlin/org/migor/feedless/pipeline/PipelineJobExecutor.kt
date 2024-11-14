package org.migor.feedless.pipeline

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
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.session.RequestContext
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.corrId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
@Transactional(propagation = Propagation.NEVER)
class PipelineJobExecutor internal constructor(
  val sourceDAO: SourceDAO,
  val documentPipelineJobDAO: DocumentPipelineJobDAO,
  val sourcePipelineJobDAO: SourcePipelineJobDAO,
  val transactionManager: PlatformTransactionManager,
  val documentDAO: DocumentDAO,
  val repositoryDAO: RepositoryDAO,
  val documentService: DocumentService,
  val sourceService: SourceService
) {

  private val log = LoggerFactory.getLogger(PipelineJobExecutor::class.simpleName)

  @Scheduled(fixedDelay = 2245, initialDelay = 20000)
  @Transactional
  fun processDocumentJobs() {
    val corrId = newCorrId()
    val groupedDocuments = documentPipelineJobDAO.findAllPendingBatched(LocalDateTime.now())
      .groupBy { it.documentId }

    if (groupedDocuments.isNotEmpty()) {
      val semaphore = Semaphore(5)
      runBlocking {
        runCatching {
          coroutineScope {
            groupedDocuments.map { groupedDocuments ->
              try {
                val userId = getOwnerIdForDocumentId(groupedDocuments.key)
                async(RequestContext(userId = userId)) {
                  semaphore.acquire()
                  delay(300)
                  try {
                    processDocumentPlugins(groupedDocuments.key, groupedDocuments.value)
                  } finally {
                    semaphore.release()
                  }
                }
              } catch (e: Exception) {
                async {}
              }
            }.awaitAll()
          }
          log.debug("[$corrId] batch refresh done")
        }.onFailure {
          log.error("[$corrId] batch refresh done: ${it.message}")
        }
      }
    }
  }

  @Scheduled(fixedDelay = 3245, initialDelay = 20000)
  @Transactional
  fun processSourceJobs() {
    val corrId = newCorrId()
    val groupedSources = sourcePipelineJobDAO.findAllPendingBatched(LocalDateTime.now())
      .groupBy { it.sourceId }

    if (groupedSources.isNotEmpty()) {
      val semaphore = Semaphore(5)
      runBlocking {
        runCatching {
          coroutineScope {
            groupedSources.map { groupedSources ->
              try {
                val userId = getOwnerIdForSourceId(groupedSources.key)
                async(RequestContext(userId = userId)) {
                  semaphore.acquire()
                  delay(300)
                  try {
                    processSourcePipeline(groupedSources.key, groupedSources.value)
                  } finally {
                    semaphore.release()
                  }
                }
              } catch (e: Exception) {
                async {}
              }
            }.awaitAll()
          }
          log.debug("[$corrId] batch refresh done")
        }.onFailure {
          log.error("[$corrId] batch refresh done: ${it.message}", it)
        }
      }
    }
  }

  private suspend fun getOwnerIdForDocumentId(documentId: UUID): UUID {
    val repo = withContext(Dispatchers.IO) {
      repositoryDAO.findByDocumentId(documentId) ?: throw failAfterCleaningJobsForDocument(documentId)
    }
    return repo.ownerId
  }

  private suspend fun failAfterCleaningJobsForDocument(documentId: UUID): IllegalArgumentException {
    withContext(Dispatchers.IO) {
      try {
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.executeWithoutResult {
          runBlocking {
            documentPipelineJobDAO.deleteAllByDocumentIdIn(listOf(documentId))
          }
        }
      } catch (e: Exception) {
        log.warn("job cleanup of document $documentId failed: ${e.message}")
      }
    }
    return IllegalArgumentException("repo not found by documentId=$documentId")
  }

  private suspend fun getOwnerIdForSourceId(sourceId: UUID): UUID {
    val repo = withContext(Dispatchers.IO) {
      repositoryDAO.findBySourceId(sourceId) ?: throw failAfterCleaningJobsForSource(sourceId)
    }
    return repo.ownerId
  }

  private suspend fun failAfterCleaningJobsForSource(sourceId: UUID): IllegalArgumentException {
    withContext(Dispatchers.IO) {
      try {
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.executeWithoutResult {
          runBlocking {
            sourcePipelineJobDAO.deleteBySourceId(sourceId)
          }
        }
      } catch (e: Exception) {
        log.warn("job cleanup of source $sourceId failed: ${e.message}")
      }
    }
    return IllegalArgumentException("repo not found by sourceId=$sourceId")
  }


  private suspend fun processSourcePipeline(sourceId: UUID, jobs: List<SourcePipelineJobEntity>) {
    val corrId = coroutineContext.corrId()
    try {
      sourceService.processSourcePipeline(sourceId, jobs)
    } catch (t: Throwable) {
      if (t !is ResumableHarvestException) {
        log.error("[$corrId] processDocumentPlugins fatal failure", t)
        withContext(Dispatchers.IO) {
          sourceDAO.setErrorState(sourceId, true, t.message)
        }
      }
    }
  }

  private suspend fun processDocumentPlugins(documentId: UUID, jobs: List<DocumentPipelineJobEntity>) {
    try {
      documentService.processDocumentPlugins(documentId, jobs)
    } catch (t: Throwable) {
      val corrId = coroutineContext.corrId()
      log.error("[$corrId] processDocumentPlugins fatal failure", t)
      withContext(Dispatchers.IO) {
        documentDAO.deleteById(documentId)
      }
    }
  }
}
