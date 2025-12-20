package org.migor.feedless.document

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.asynchttpclient.exception.TooManyConnectionsPerHostException
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.capability.CapabilityId
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.common.PropertyService
import org.migor.feedless.generated.types.CreateRecordInput
import org.migor.feedless.generated.types.RecordUpdateInput
import org.migor.feedless.message.MessageService
import org.migor.feedless.pipeline.FilterEntityPlugin
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.pipeline.Plugin
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipeline.ReportPlugin
import org.migor.feedless.pipeline.plugins.StringFilter
import org.migor.feedless.pipeline.plugins.asJsonItem
import org.migor.feedless.pipelineJob.DocumentPipelineJob
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.pipelineJob.PipelineJobStatus
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.toJsonItem
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.transport.TelegramBotService
import org.migor.feedless.user.userId
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.jvm.optionals.getOrNull


@Service
@Profile("${AppProfiles.document} & ${AppLayer.service}")
class DocumentUseCase(
  private val documentRepository: DocumentRepository,
  private val repositoryRepository: RepositoryRepository,
  private val planConstraintsService: PlanConstraintsService,
  private val documentPipelineJobRepository: DocumentPipelineJobRepository,
  private val pluginService: PluginService,
  private val telegramBotServiceMaybe: Optional<TelegramBotService>,
  private val messageService: MessageService,
  private val propertyService: PropertyService,
  private val documentGuard: DocumentGuard,
  private val repositoryGuard: RepositoryGuard,
) : DocumentProvider {

  private val log = LoggerFactory.getLogger(DocumentUseCase::class.simpleName)

  suspend fun findById(id: DocumentId): Document? = withContext(Dispatchers.IO) {
    log.info("findById id=$id")
    documentRepository.findById(id)
  }

  suspend fun findAllByRepositoryId(
    repositoryId: RepositoryId,
    filter: DocumentsFilter? = null,
    orderBy: RecordOrderBy? = null,
    status: ReleaseStatus = ReleaseStatus.released,
    tags: List<String> = emptyList(),
    pageable: PageableRequest,
  ): List<Document> = withContext(Dispatchers.IO) {
    log.info("findAllByRepositoryId repositoryId=$repositoryId")

    repositoryGuard.requireRead(repositoryId)

    documentRepository.findAllFiltered(repositoryId, filter, orderBy, status, tags, pageable)
  }

  fun applyRetentionStrategyByCapacity() {
    log.info("applyRetentionStrategyByCapacity")
    repositoryRepository.findAllByLastUpdatedAtBefore(LocalDateTime.now().minusDays(1))
      .forEach { repository ->
        val retentionSize = planConstraintsService.coerceRetentionMaxCapacity(
          repository.retentionMaxCapacity,
          groupId = repository.groupId
        )
        if (retentionSize != null && retentionSize > 0) {
          log.info("applying retention for repo ${repository.id} with maxItems=$retentionSize")
          documentRepository.deleteAllByRepositoryIdAndStatusWithSkip(
            repository.id,
            ReleaseStatus.released,
            retentionSize
          )
          documentRepository.deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
            repository.id,
            LocalDateTime.now().minusDays(7),
            ReleaseStatus.unreleased
          )
        } else {
          log.debug("no retention with maxItems given repo ${repository.id}")
        }
      }
  }

  suspend fun applyRetentionStrategy(repositoryId: RepositoryId) = withContext(Dispatchers.IO) {
    log.info("applyRetentionStrategy repositoryId=$repositoryId")
    val repository = repositoryRepository.findById(repositoryId)!!

    repositoryGuard.requireRead(repositoryId)

    planConstraintsService.coerceRetentionMaxAgeDays(
      repository.retentionMaxAgeDays,
      repository.groupId,
    )
      ?.let { maxAgeDays ->
        log.info("applying retention with maxAgeDays=$maxAgeDays using field ${repository.retentionMaxAgeDaysReferenceField}")
        val maxDate = LocalDateTime.now().minus(maxAgeDays.toLong(), ChronoUnit.DAYS)
        if (repository.retentionMaxAgeDaysReferenceField == MaxAgeDaysDateField.startingAt) {
          documentRepository.deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(
            repository.id,
            maxDate,
            ReleaseStatus.released
          )
        } else {
          if (repository.retentionMaxAgeDaysReferenceField == MaxAgeDaysDateField.createdAt) {
            documentRepository.deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
              repository.id,
              maxDate,
              ReleaseStatus.released
            )
          } else {
            documentRepository.deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(
              repository.id,
              maxDate,
              ReleaseStatus.released
            )
          }
        }
      } ?: log.debug("no retention with maxAgeDays given")
  }

  suspend fun deleteDocuments(repositoryId: RepositoryId, documentIds: StringFilter) = withContext(Dispatchers.IO) {
    log.info("deleteDocuments $documentIds")

    repositoryGuard.requireRead(repositoryId)

    val repository = repositoryRepository.findById(repositoryId)!!
    if (repository.ownerId != coroutineContext.userId()) {
      throw PermissionDeniedException("current user ist not owner")
    }

    val documents = documentRepository
      .findAllByRepositoryIdAndIdIn(
        repositoryId, if (documentIds.`in` != null) {
          documentIds.`in`.map { DocumentId(it) }
        } else {
          if (documentIds.eq != null) {
            listOf(DocumentId(documentIds.eq))
          } else {
            throw IllegalArgumentException("operation not supported")
          }
        }
      )

    documentRepository.deleteAllById(documents.map { it.id });
  }

  suspend fun getRecordFrequency(
    where: DocumentsFilter,
    groupBy: DocumentDateField,
  ): List<DocumentFrequency> = withContext(Dispatchers.IO) {
    log.info("getRecordFrequency groupBy=$groupBy")
    documentRepository.getRecordFrequency(where, groupBy)
  }

  suspend fun processDocumentPlugins(documentId: DocumentId, jobs: List<DocumentPipelineJob>): Document? =
    withContext(Dispatchers.IO) {
      log.info("processDocumentPlugins documentId=$documentId jobs=${jobs.size}")
      val document = documentRepository.findByIdWithSource(documentId)!!
      val logCollector = LogCollector()

      try {
        val repository = repositoryRepository.findById(document.repositoryId)!!

        data class ProcessingState(
          val currentDocument: Document,
          val shouldContinue: Boolean,
          val hasError: Boolean
        )

        val finalState =
          jobs.fold(ProcessingState(document, shouldContinue = true, hasError = false)) { state, job ->
            if (!state.shouldContinue) {
              return@fold state
            }

            try {
              val updatedDocument =
                when (val plugin = pluginService.resolveById<Plugin>(job.pluginId)) {
                  is FilterEntityPlugin<*> -> {
                    if (!plugin.filterEntity(
                        state.currentDocument.toJsonItem(
                          propertyService,
                          EntityVisibility.isPublic
                        ),
                        job.executorParams.paramsJsonString,
                        0,
                        logCollector
                      )
                    ) {
                      throw FilterMismatchException()
                    }
                    state.currentDocument
                  }

                  is MapEntityPlugin<*> -> plugin.mapEntity(
                    state.currentDocument,
                    repository,
                    job.executorParams.paramsJsonString,
                    logCollector
                  )

                  is ReportPlugin -> {
                    log.info("ignoring ${plugin.id()} plugin")
                    state.currentDocument
                  }

                  else -> {
                    if (plugin == null) {
                      log.error(
                        "Invalid pluginId '${job.pluginId}'. Available: [${
                          pluginService.findAll().joinToString(", ") { "'${it.id()}'" }
                        }]")
                    } else {
                      log.warn("resolved unsupported plugin ${plugin}")
                    }
                    throw IllegalArgumentException("Invalid plugin")
                  }
                }

              log.debug("executed ${job.pluginId} for $documentId")
              documentPipelineJobRepository.save(job.copy(status = PipelineJobStatus.SUCCEEDED))
              ProcessingState(updatedDocument, true, false)
            } catch (e: Exception) {
              if (e is ResumableHarvestException || e is TooManyConnectionsPerHostException) {
                delayJob(job, e, state.currentDocument)


              } else {
                if (e !is FilterMismatchException) {
                  log.warn("${e::class.simpleName} ${e.message}")
                } else {
                  log.info("${e::class.simpleName} ${e.message}")
                }
                deleteDocument(state.currentDocument)
              }
              ProcessingState(state.currentDocument, false, true)
            }
          }

        if (!finalState.hasError) {
          releaseDocument(finalState.currentDocument, repository)
          finalState.currentDocument
        } else {
          null
        }

      } catch (throwable: Throwable) {
        log.warn("aborting pipeline for document, cause ${throwable.message}")
        deleteDocument(document)
        null
      }
    }

  private suspend fun releaseDocument(
    document: Document,
    repository: Repository,
  ) {
//    forwardToMail(corrId, document, repository)
    log.info("releasing document ${document.id}")

    documentRepository.save(
      document.copy(
        status = ReleaseStatus.released
      )
    )

    triggerPostReleaseEffects(listOf(document), repository)
  }

  suspend fun triggerPostReleaseEffects(
    documents: List<Document>,
    repository: Repository,
  ) {
    log.info("triggerPostReleaseEffects documents=${documents.size} repositoryId=${repository.id}")
    if (repository.pushNotificationsEnabled) {
      telegramBotServiceMaybe.getOrNull()?.let { telegramBot ->
        telegramBot.findByUserIdAndAuthorizedIsTrue(repository.ownerId)?.let { telegramLink ->
          documents.forEach {
            messageService.publishMessage(
              TelegramBotService.toTopic(telegramLink.chatId!!),
              it.asJsonItem(repository)
            )
          }
        }
      }
    }
  }

  private suspend fun deleteDocument(document: Document) = withContext(Dispatchers.IO) {
    log.info("delete document ${document.id}")
    documentRepository.deleteById(document.id)
  }

  private suspend fun delayJob(
    job: DocumentPipelineJob,
    e: Exception,
    document: Document,
  ) = withContext(Dispatchers.IO) {
    log.info("delaying ${job.documentId} (${job.pluginId})")

    documentPipelineJobRepository.save(
      job.copy(
        coolDownUntil = if (e is ResumableHarvestException) {
          LocalDateTime.now().plus(e.nextRetryAfter)
        } else {
          LocalDateTime.now().plusSeconds(120)
        }
      )
    )
    documentRepository.save(document) // todo still needed?
  }

  suspend fun countByRepositoryId(repositoryId: RepositoryId): Long = withContext(Dispatchers.IO) {
    log.info("countByRepositoryId repositoryId=$repositoryId")
    documentRepository.countByRepositoryId(repositoryId)
  }

  suspend fun createDocument(data: CreateRecordInput): Document = withContext(Dispatchers.IO) {
    log.info("createDocument repositoryId=${data.repositoryId.id}")
    val repositoryId = RepositoryId(data.repositoryId.id)

    val repository = repositoryGuard.requireWrite(repositoryId)

    val documentId = data.id?.let { DocumentId(data.id!!) } ?: DocumentId()
    val document = Document(
      id = documentId,
      title = data.title,
      url = data.url,
      text = data.text!!,
      repositoryId = repositoryId,
      status = ReleaseStatus.released,
      contentHash = CryptUtil.sha1(data.rawBase64 ?: documentId.uuid.toString())
    )

    documentRepository.save(document)
  }

  suspend fun updateDocument(data: RecordUpdateInput, id: DocumentId): Document = withContext(Dispatchers.IO) {
    log.info("updateDocument id=$id")
    var document = documentRepository.findById(id)!!
      .copy(
        updatedAt = LocalDateTime.now()
      )

    repositoryGuard.requireWrite(document.repositoryId)

    document = data.url?.let {
      document.copy(url = it.set)
    } ?: document

    document = data.text?.let {
      document.copy(text = it.set)
    } ?: document

    document = data.title?.let {
      document.copy(title = it.set)
    } ?: document

    documentRepository.save(document)
  }

  suspend fun findFirstByContentHashOrUrlAndRepositoryId(
    contentHash: String,
    url: String,
    repositoryId: RepositoryId
  ): Document? = withContext(Dispatchers.IO) {
    log.info("findFirstByContentHashOrUrlAndRepositoryId repositoryId=$repositoryId")
    documentRepository.findFirstByContentHashOrUrlAndRepositoryId(contentHash, url, repositoryId)
  }

  override suspend fun expectsCapabilities(capabilityId: CapabilityId): Boolean {
    log.info("expectsCapabilities capabilityId=$capabilityId")
    return UserCapability.ID === capabilityId
  }

  override suspend fun provideAll(
    capability: UnresolvedCapability,
    pageable: PageableRequest,
    filter: DocumentsFilter,
    order: RecordOrderBy,
  ): List<Document> {
    log.info("provideAll")
    val userCapability = UserCapability.resolve(capability)
    return findAllByRepositoryId(filter.repository, filter, order, pageable = pageable)
  }

//  private suspend fun forwardToMail(document: DocumentEntity, repository: RepositoryEntity) {
//    val mailForwards = withContext(Dispatchers.IO) {
//      mailForwardDAO.findAllByRepositoryId(repository.id)
//    }
//    if (mailForwards.isNotEmpty()) {
//      val authorizedMailForwards =
//        mailForwards.filterTo(ArrayList()) { it: MailForwardEntity -> it.authorized }.map { it.email }
//      if (authorizedMailForwards.isEmpty()) {
//        log.warn("no authorized mail-forwards available of ${mailForwards.size}")
//      } else {
//        val (mailFormatter, params) = pluginService.resolveMailFormatter(repository)
//        log.debug("using formatter ${mailFormatter::class.java.name}")
//
//        val from = mailService.getNoReplyAddress(repository.product)
//        val to = authorizedMailForwards.toTypedArray()
//
//        log.debug("resolved mail recipients [${authorizedMailForwards.joinToString(", ")}]")
//        mailService.send(
//          corrId,
//          from,
//          to,
//          mailFormatter.provideDocumentMail(corrId, document, repository, params)
//        )
//      }
//    }
//  }

}

class FilterMismatchException : RuntimeException()


