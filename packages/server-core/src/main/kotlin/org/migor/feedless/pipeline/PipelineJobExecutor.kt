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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
@Transactional(propagation = Propagation.NEVER)
class PipelineJobExecutor internal constructor() {

  @Autowired
  private lateinit var sourceDAO: SourceDAO
  private val log = LoggerFactory.getLogger(PipelineJobExecutor::class.simpleName)

  @Autowired
  private lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO

  @Autowired
  private lateinit var sourcePipelineJobDAO: SourcePipelineJobDAO

  @Autowired
  private lateinit var documentDAO: DocumentDAO

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var documentService: DocumentService

  @Autowired
  private lateinit var sourceService: SourceService

  @Scheduled(fixedDelay = 6245, initialDelay = 20000)
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
              async(RequestContext(userId = getOwnerIdForDocumentId(groupedDocuments.key))) {
                semaphore.acquire()
                delay(2000)
                try {
                  processDocumentPlugins(groupedDocuments.key, groupedDocuments.value)
                } finally {
                  semaphore.release()
                }
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

    @Scheduled(fixedDelay = 5245, initialDelay = 20000)
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
              async(RequestContext(userId = getOwnerIdForSourceId(groupedSources.key))) {
                semaphore.acquire()
                try {
                  processSourcePipeline(groupedSources.key, groupedSources.value)
                } finally {
                  semaphore.release()
                }
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
      repositoryDAO.findByDocumentId(documentId) ?: throw IllegalArgumentException("repo not found by documentId")
    }
    return repo.ownerId
  }

  private suspend fun getOwnerIdForSourceId(sourceId: UUID): UUID {
    val repo = withContext(Dispatchers.IO) {
      repositoryDAO.findBySourceId(sourceId) ?: throw IllegalArgumentException("repo not found by sourceId")
    }
    return repo.ownerId
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
