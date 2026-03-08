package org.migor.feedless.segmentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentProvider
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.message.MessageService
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.transport.TelegramBotService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*


@Service
@Profile("${AppProfiles.document} & ${AppLayer.service}")
class SegmentationUseCase(
  private val documentRepository: DocumentRepository,
  private val repositoryRepository: RepositoryRepository,
  private val planConstraintsService: PlanConstraintsService,
  private val documentPipelineJobRepository: DocumentPipelineJobRepository,
  private val pluginService: PluginService,
  private val telegramBotServiceMaybe: Optional<TelegramBotService>,
  private val messageService: MessageService,
  private val propertyService: PropertyService,
  private val repositoryGuard: RepositoryGuard,
) : DocumentProvider {

  private val log = LoggerFactory.getLogger(SegmentationUseCase::class.simpleName)

  suspend fun findById(id: DocumentId): Document? = withContext(Dispatchers.IO) {
    log.info("findById id=$id")
    documentRepository.findById(id)
  }

  suspend fun processDocumentJobs() {
    try {
      val groupedDocuments = withContext(Dispatchers.IO) {
        documentPipelineJobRepository.findAllPendingBatched(LocalDateTime.now())
          .groupBy { it.documentId }
      }

      // todo fix and use
//      withContext(Dispatchers.IO) {
//        documentPipelineRepository.incrementAttemptCount(groupedDocuments.values.flatten().map { it.id })
//      }

      if (groupedDocuments.isNotEmpty()) {
        val semaphore = Semaphore(5)
        runCatching {
          coroutineScope {
            groupedDocuments.map { groupedDocuments ->
              try {
                val repository = getRepositoryForDocumentId(groupedDocuments.key)
                async(RequestContext(userId = repository.ownerId, groupId = repository.groupId)) {
                  semaphore.acquire()
                  delay(300)
                  try {
                    processDocumentPlugins(groupedDocuments.key, groupedDocuments.value)
                  } catch (t: Throwable) {
                    log.error("processDocumentPlugins fatal failure", t)
                    withContext(Dispatchers.IO) {
                      documentRepository.deleteById(groupedDocuments.key)
                    }
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
    } catch (e: Exception) {
      log.error(e.message, e)
    }
  }
}


