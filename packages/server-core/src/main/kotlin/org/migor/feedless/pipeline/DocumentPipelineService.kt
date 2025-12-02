package org.migor.feedless.pipeline

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentId
import org.migor.feedless.pipelineJob.DocumentPipelineJob
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class DocumentPipelineService(
  val documentPipelineJobRepository: DocumentPipelineJobRepository,
) {

  private val log = LoggerFactory.getLogger(DocumentPipelineService::class.simpleName)

  @Transactional
  suspend fun incrementDocumentJobAttemptCount(groupedDocuments: Map<DocumentId, List<DocumentPipelineJob>>) {
    documentPipelineJobRepository.incrementAttemptCount(groupedDocuments.values.flatMap {
      it.map { it.id }
    }.distinct())
  }

  @Transactional
  suspend fun failAfterCleaningJobsForDocument(documentId: DocumentId): IllegalArgumentException {
    withContext(Dispatchers.IO) {
      try {
        log.info("clean jobs for document $documentId")
        documentPipelineJobRepository.deleteAllByDocumentIdIn(listOf(documentId))
      } catch (e: Exception) {
        log.warn("job cleanup of document $documentId failed: ${e.message}")
      }
    }
    return IllegalArgumentException("repo not found by documentId=$documentId")
  }

  @Transactional(readOnly = true)
  suspend fun findAllPendingBatched(now: LocalDateTime): List<DocumentPipelineJob> {
    return documentPipelineJobRepository.findAllPendingBatched(now)
  }

  @Transactional
  suspend fun deleteAllByCreatedAtBefore(refDate: LocalDateTime) {
    documentPipelineJobRepository.deleteAllByCreatedAtBefore(refDate)
  }

  @Transactional
  suspend fun saveAll(jobs: List<DocumentPipelineJob>): List<DocumentPipelineJob> {
    return documentPipelineJobRepository.saveAll(jobs)
  }

  @Transactional
  suspend fun deleteAllByDocumentIdIn(documentIds: List<DocumentId>) {
    withContext(Dispatchers.IO) {
      documentPipelineJobRepository.deleteAllByDocumentIdIn(documentIds)
    }
  }
}
