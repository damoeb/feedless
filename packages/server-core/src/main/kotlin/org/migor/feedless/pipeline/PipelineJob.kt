package org.migor.feedless.pipeline

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
class PipelineJob internal constructor() {

  @Autowired
  private lateinit var sourceDAO: SourceDAO
  private val log = LoggerFactory.getLogger(PipelineJob::class.simpleName)

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
    documentPipelineJobDAO.findAllPendingBatched()
      .groupBy { it.documentId }
      .map { processDocumentPlugins(newCorrId(3, corrId), it.key, it.value) }
  }

  @Scheduled(fixedDelay = 5245, initialDelay = 20000)
  @Transactional
  fun processSourceJobs() {
    val corrId = newCorrId()
    sourcePipelineJobDAO.findAllPendingBatched()
      .groupBy { it.sourceId }
      .map { processSourcePipeline(newCorrId(3, corrId), it.key, it.value) }
  }

  private fun processSourcePipeline(corrId: String, sourceId: UUID, jobs: List<SourcePipelineJobEntity>) {
    try {
      sourceService.processSourcePipeline(corrId, sourceId, jobs)
    } catch (t: Throwable) {
      if (t !is ResumableHarvestException) {
        log.warn("[$corrId] processSourcePipeline failed ${t.message}]")
        sourceDAO.setErrorState(sourceId, true, t.message)
      }
    }
  }

  private fun processDocumentPlugins(corrId: String, documentId: UUID, jobs: List<DocumentPipelineJobEntity>) {
    try {
      documentService.processDocumentPlugins(corrId, documentId, jobs)
    } catch (t: Throwable) {
      log.warn("[$corrId] processDocumentPlugins failed: ${t.message}")
      documentDAO.deleteById(documentId)
    }
  }
}
