package org.migor.feedless.pipeline

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.pipelineJob.DocumentPipelineJob
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.UserId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class DocumentPipelineJobExecutor internal constructor(
  val documentPipelineRepository: DocumentPipelineJobRepository,
  val documentRepository: DocumentRepository,
  val repositoryUseCase: RepositoryUseCase,
  val repositoryRepository: RepositoryRepository,
  val documentUseCase: DocumentUseCase,
) {

  private val log = LoggerFactory.getLogger(DocumentPipelineJobExecutor::class.simpleName)

  //  @Scheduled(fixedDelay = 2245, initialDelay = 20000)
  @Transactional
  fun processDocumentJobs() {
    try {
      val groupedDocuments = documentPipelineRepository.findAllPendingBatched(LocalDateTime.now())
        .groupBy { it.documentId }

      documentPipelineRepository.incrementAttemptCount(groupedDocuments.values.flatten().map { it.id })

      if (groupedDocuments.isNotEmpty()) {
        val semaphore = Semaphore(5)
        runBlocking {
          runCatching {
            coroutineScope {
              groupedDocuments.map { groupedDocuments ->
                try {
                  val userId = getOwnerIdForDocumentId(groupedDocuments.key)
                  async(RequestContext(userId = userId, corrId = newCorrId())) {
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
            log.info("done")
          }.onFailure {
            log.error("batch refresh done: ${it.message}")
          }
        }
      }
    } catch (e: Exception) {
      log.error(e.message, e)
    }
  }

  private suspend fun getOwnerIdForDocumentId(documentId: DocumentId): UserId {
    val repo = repositoryRepository.findByDocumentId(documentId)
      ?: throw IllegalArgumentException("repo not found for documentId $documentId") // documentPipelineService.failAfterCleaningJobsForDocument(documentId)
    return repo.ownerId
  }

  private suspend fun processDocumentPlugins(documentId: DocumentId, jobs: List<DocumentPipelineJob>) {
    try {
      documentUseCase.processDocumentPlugins(documentId, jobs)
    } catch (t: Throwable) {
      log.error("processDocumentPlugins fatal failure", t)
      documentRepository.deleteById(documentId)
    }
  }
}
