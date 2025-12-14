package org.migor.feedless.pipeline

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class DocumentPipelineService(
  private val documentPipelineJobRepository: DocumentPipelineJobRepository,
) {

  private val log = LoggerFactory.getLogger(DocumentPipelineService::class.simpleName)

//  suspend fun incrementDocumentJobAttemptCount(documentJobs: List<DocumentPipelineJob>) =
//    withContext(Dispatchers.IO) {
//      documentPipelineJobRepository.incrementAttemptCount(documentJobs.map { it.id }.distinct())
//    }

//  suspend fun failAfterCleaningJobsForDocument(documentId: DocumentId): IllegalArgumentException =
//    withContext(Dispatchers.IO) {
//      try {
//        log.info("clean jobs for document $documentId")
//        documentPipelineJobRepository.deleteAllByDocumentIdIn(listOf(documentId))
//      } catch (e: Exception) {
//        log.warn("job cleanup of document $documentId failed: ${e.message}")
//      }
//      IllegalArgumentException("repo not found by documentId=$documentId")
//    }

}
