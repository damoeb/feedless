package org.migor.feedless.pipeline

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentId
import org.migor.feedless.jpa.documentPipelineJob.DocumentPipelineJobDAO
import org.migor.feedless.jpa.documentPipelineJob.DocumentPipelineJobEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class DocumentPipelineService internal constructor(
  val documentPipelineJobDAO: DocumentPipelineJobDAO,
) {

  private val log = LoggerFactory.getLogger(DocumentPipelineService::class.simpleName)

  @Transactional
  fun incrementDocumentJobAttemptCount(groupedDocuments: Map<UUID, List<DocumentPipelineJobEntity>>) {
    documentPipelineJobDAO.incrementAttemptCount(groupedDocuments.keys.distinct())
  }

  @Transactional
  suspend fun failAfterCleaningJobsForDocument(documentId: DocumentId): IllegalArgumentException {
    withContext(Dispatchers.IO) {
      try {
        log.info("clean jobs for document $documentId")
        documentPipelineJobDAO.deleteAllByDocumentIdIn(listOf(documentId.value))
      } catch (e: Exception) {
        log.warn("job cleanup of document $documentId failed: ${e.message}")
      }
    }
    return IllegalArgumentException("repo not found by documentId=$documentId")
  }

  @Transactional(readOnly = true)
  fun findAllPendingBatched(now: LocalDateTime): List<DocumentPipelineJobEntity> {
    return documentPipelineJobDAO.findAllPendingBatched(now)
  }

  @Transactional
  fun deleteAllByCreatedAtBefore(refDate: LocalDateTime) {
    documentPipelineJobDAO.deleteAllByCreatedAtBefore(refDate)
  }

  @Transactional
  suspend fun saveAll(jobs: List<DocumentPipelineJobEntity>): List<DocumentPipelineJobEntity> {
    return withContext(Dispatchers.IO) {
      documentPipelineJobDAO.saveAll(jobs)
    }
  }

  @Transactional
  suspend fun deleteAllByDocumentIdIn(jobs: List<UUID>) {
    withContext(Dispatchers.IO) {
      documentPipelineJobDAO.deleteAllByDocumentIdIn(jobs)
    }
  }
}
