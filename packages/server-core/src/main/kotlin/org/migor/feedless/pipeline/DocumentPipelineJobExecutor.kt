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
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.corrId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
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
class DocumentPipelineJobExecutor internal constructor(
  val documentPipelineJobDAO: DocumentPipelineJobDAO,
  val documentDAO: DocumentDAO,
  val repositoryDAO: RepositoryDAO,
  val documentService: DocumentService,
) {

  private val log = LoggerFactory.getLogger(DocumentPipelineJobExecutor::class.simpleName)

  @Scheduled(fixedDelay = 2245, initialDelay = 20000)
  @Transactional
  fun processDocumentJobs() {
    try {
      val corrId = newCorrId()
      val groupedDocuments = documentPipelineJobDAO.findAllPendingBatched(LocalDateTime.now())
        .groupBy { it.documentId }

      incrementDocumentJobAttemptCount(groupedDocuments)

      if (groupedDocuments.isNotEmpty()) {
        val semaphore = Semaphore(5)
        runBlocking {
          runCatching {
            coroutineScope {
              groupedDocuments.map { groupedDocuments ->
                try {
                  val userId = getOwnerIdForDocumentId(groupedDocuments.key)
                  async(RequestContext(userId = userId, corrId = corrId)) {
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
            log.info("[$corrId] done")
          }.onFailure {
            log.error("[$corrId] batch refresh done: ${it.message}")
          }
        }
      }
    } catch (e: Exception) {
      log.error(e.message)
    }
  }

  private fun incrementDocumentJobAttemptCount(groupedDocuments: Map<UUID, List<DocumentPipelineJobEntity>>) {
    documentPipelineJobDAO.incrementAttemptCount(groupedDocuments.keys.distinct())
  }

  private suspend fun getOwnerIdForDocumentId(documentId: UUID): UUID {
    val repo = withContext(Dispatchers.IO) {
      repositoryDAO.findByDocumentId(documentId)
    } ?: throw failAfterCleaningJobsForDocument(documentId)
    return repo.ownerId
  }

  private suspend fun failAfterCleaningJobsForDocument(documentId: UUID): IllegalArgumentException {
    withContext(Dispatchers.IO) {
      try {
        log.info("clean jobs for document $documentId")
        documentPipelineJobDAO.deleteAllByDocumentIdIn(listOf(documentId))
      } catch (e: Exception) {
        log.warn("job cleanup of document $documentId failed: ${e.message}")
      }
    }
    return IllegalArgumentException("repo not found by documentId=$documentId")
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
