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
import org.migor.feedless.data.jpa.pipelineJob.DocumentPipelineJobEntity
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentService
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.UserId
import org.migor.feedless.user.corrId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class DocumentPipelineJobExecutor internal constructor(
  val documentPipelineService: DocumentPipelineService,
  val repositoryService: RepositoryService,
  val documentService: DocumentService,
) {

  private val log = LoggerFactory.getLogger(DocumentPipelineJobExecutor::class.simpleName)

  @Scheduled(fixedDelay = 2245, initialDelay = 20000)
  fun processDocumentJobs() {
    try {
      val corrId = newCorrId()
      val groupedDocuments = documentPipelineService.findAllPendingBatched(LocalDateTime.now())
        .groupBy { it.documentId }

      documentPipelineService.incrementDocumentJobAttemptCount(groupedDocuments)

      if (groupedDocuments.isNotEmpty()) {
        val semaphore = Semaphore(5)
        runBlocking {
          runCatching {
            coroutineScope {
              groupedDocuments.map { groupedDocuments ->
                try {
                  val userId = getOwnerIdForDocumentId(DocumentId(groupedDocuments.key))
                  async(RequestContext(userId = userId, corrId = corrId)) {
                    semaphore.acquire()
                    delay(300)
                    try {
                      processDocumentPlugins(DocumentId(groupedDocuments.key), groupedDocuments.value)
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

  private suspend fun getOwnerIdForDocumentId(documentId: DocumentId): UserId {
    val repo = withContext(Dispatchers.IO) {
      repositoryService.findByDocumentId(documentId.value)
    } ?: throw documentPipelineService.failAfterCleaningJobsForDocument(documentId)
    return repo.ownerId
  }

  private suspend fun processDocumentPlugins(documentId: DocumentId, jobs: List<DocumentPipelineJobEntity>) {
    try {
      documentService.processDocumentPlugins(documentId, jobs)
    } catch (t: Throwable) {
      val corrId = coroutineContext.corrId()
      log.error("[$corrId] processDocumentPlugins fatal failure", t)
      documentService.deleteById(documentId)
    }
  }
}
