package org.migor.feedless.pipeline

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentService
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("${AppProfiles.database} & ${AppProfiles.cron}")
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
  private lateinit var documentService: DocumentService

  @Autowired
  private lateinit var sourceService: SourceService

  @Scheduled(fixedDelay = 6245, initialDelay = 20000)
  @Transactional
  fun processDocumentJobs() {
    val corrId = newCorrId()
    val groupedDocuments = documentPipelineJobDAO.findAllPendingBatched()
      .groupBy { it.documentId }

    if (groupedDocuments.isNotEmpty()) {
      val semaphore = Semaphore(5)
      runBlocking {
        runCatching {
          coroutineScope {
            groupedDocuments.map {
              async(Dispatchers.Unconfined) {
                semaphore.acquire()
                delay(2000)
                try {
                  processDocumentPlugins(newCorrId(3, corrId), it.key, it.value)
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

//  @Scheduled(fixedDelay = 5245, initialDelay = 20000)
  @Transactional
  fun processSourceJobs() {
    val corrId = newCorrId()
    val groupedSources = sourcePipelineJobDAO.findAllPendingBatched()
      .groupBy { it.sourceId }

    if (groupedSources.isNotEmpty()) {
      val semaphore = Semaphore(5)
      runBlocking {
        runCatching {
          coroutineScope {
            groupedSources.map {
              async(Dispatchers.Unconfined) {
                semaphore.acquire()
                try {
                  processSourcePipeline(newCorrId(3, corrId), it.key, it.value)
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

  private suspend fun processSourcePipeline(corrId: String, sourceId: UUID, jobs: List<SourcePipelineJobEntity>) {
    try {
      sourceService.processSourcePipeline(corrId, sourceId, jobs)
    } catch (t: Throwable) {
      if (t !is ResumableHarvestException) {
        log.error("[$corrId] processDocumentPlugins fatal failure", t)
        withContext(Dispatchers.IO) {
          sourceDAO.setErrorState(sourceId, true, t.message)
        }
      }
    }
  }

  private suspend fun processDocumentPlugins(corrId: String, documentId: UUID, jobs: List<DocumentPipelineJobEntity>) {
    try {
      documentService.processDocumentPlugins(corrId, documentId, jobs)
    } catch (t: Throwable) {
      log.error("[$corrId] processDocumentPlugins fatal failure", t)
      withContext(Dispatchers.IO) {
        documentDAO.deleteById(documentId)
      }
    }
  }
}
