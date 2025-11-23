package org.migor.feedless.document

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Path
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.asynchttpclient.exception.TooManyConnectionsPerHostException
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.document.DocumentEntity
import org.migor.feedless.generated.types.CreateRecordInput
import org.migor.feedless.generated.types.DatesWhereInput
import org.migor.feedless.generated.types.RecordDateField
import org.migor.feedless.generated.types.RecordFrequency
import org.migor.feedless.generated.types.RecordOrderByInput
import org.migor.feedless.generated.types.RecordUpdateInput
import org.migor.feedless.generated.types.RecordsWhereInput
import org.migor.feedless.message.MessageService
import org.migor.feedless.pipeline.FeedlessPlugin
import org.migor.feedless.pipeline.FilterEntityPlugin
import org.migor.feedless.pipeline.MapEntityPlugin
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
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.toJsonItem
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.session.PermissionService
import org.migor.feedless.source.SourceId
import org.migor.feedless.transport.TelegramBotService
import org.migor.feedless.user.User
import org.migor.feedless.user.corrId
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.jvm.optionals.getOrNull


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.document} & ${AppLayer.service}")
class DocumentService(
    private val documentDAO: DocumentRepository,
    private val entityManager: EntityManager,
    private val repositoryDAO: RepositoryRepository,
    private val planConstraintsService: PlanConstraintsService,
    private val documentPipelineJobDAO: DocumentPipelineJobRepository,
    private val pluginService: PluginService,
    private val permissionService: PermissionService,
    private val telegramBotServiceMaybe: Optional<TelegramBotService>,
    private val messageService: MessageService,
    private val propertyService: PropertyService,
) {

    private val log = LoggerFactory.getLogger(DocumentService::class.simpleName)

    @Transactional(readOnly = true)
    suspend fun findById(id: DocumentId): Document? {
        return documentDAO.findById(id)
    }

    @Transactional(readOnly = true)
    suspend fun findAllByRepositoryId(
        repositoryId: RepositoryId,
        where: RecordsWhereInput? = null,
        orderBy: RecordOrderByInput? = null,
        status: ReleaseStatus = ReleaseStatus.released,
        tags: List<String> = emptyList(),
        pageable: Pageable,
    ): List<Document> {
        val query = jpql {
            val whereStatements = prepareWhereStatements(where, tags)

            select(
                path(DocumentEntity::id),
            ).from(
                entity(DocumentEntity::class),
            )
                .whereAnd(
                    path(DocumentEntity::repositoryId).eq(repositoryId.uuid),
                    path(DocumentEntity::status).`in`(status),
                    path(DocumentEntity::publishedAt).lt(LocalDateTime.now()),
                    *whereStatements.toTypedArray()
                ).orderBy(
                    orderBy?.let {
                        path(DocumentEntity::startingAt).asc().nullsLast()
                    } ?: path(DocumentEntity::publishedAt).desc()
                )
        }

        val context = JpqlRenderContext()

        return withContext(Dispatchers.IO) {
            val q = entityManager.createQuery(query, context)
            q.setMaxResults(pageable.pageSize)
            q.setFirstResult(pageable.pageSize * pageable.pageNumber)
            documentDAO.findAllWithAttachmentsByIdIn(q.resultList.map { DocumentId(it) })
        }
    }

    private fun prepareWhereStatements(
        where: RecordsWhereInput?,
        tags: List<String> = emptyList()
    ): MutableList<Predicatable> {
        val whereStatements = mutableListOf<Predicatable>()
        jpql {
            val addDateConstraint = { it: DatesWhereInput, field: Path<LocalDateTime> ->
                it.before?.let {
                    whereStatements.add(field.le(it.toLocalDateTime()))
                }
                it.after?.let {
                    whereStatements.add(field.ge(it.toLocalDateTime()))
                }
                // todo create a test
                if (it.inFuture == true) {
                    whereStatements.add(field.ge(LocalDateTime.now()))
                }
            }

            if (tags.isNotEmpty()) {
                // todo one of
            }

            where?.let {
                it.id?.eq?.let { whereStatements.add(path(DocumentEntity::id).eq(UUID.fromString(it))) }
                it.id?.`in`?.let { whereStatements.add(path(DocumentEntity::id).`in`(it.map { UUID.fromString(it) })) }
                it.source?.let { whereStatements.add(path(DocumentEntity::sourceId).eq(UUID.fromString(it.id))) }
                it.startedAt?.let { addDateConstraint(it, path(DocumentEntity::startingAt)) }
                it.createdAt?.let { addDateConstraint(it, path(DocumentEntity::createdAt)) }
                it.updatedAt?.let { addDateConstraint(it, path(DocumentEntity::updatedAt)) }
                it.publishedAt?.let { addDateConstraint(it, path(DocumentEntity::publishedAt)) }
                it.latLng?.let {
                    whereStatements.add(path(DocumentEntity::latLon).isNotNull())
                    it.near?.let {
                        // https://postgis.net/docs/ST_Distance.html
                        whereStatements.add(
                            function(
                                Double::class,
                                "fl_latlon_distance",
                                path(DocumentEntity::latLon),
                                doubleLiteral(it.point.lat),
                                doubleLiteral(it.point.lng)
                            )
                                .lt(doubleLiteral(it.distanceKm.coerceAtMost(50.0)))
                        )
                    }
                }
            }
            // dummy
            select(expression<String>("")).from(entity(DocumentEntity::class))
        }

        return whereStatements
    }

    @Transactional
    suspend fun applyRetentionStrategyByCapacity() {
        repositoryDAO.findAllByLastUpdatedAtBefore(LocalDateTime.now().minusDays(1))
            .forEach { repository ->
                val retentionSize = runBlocking {
                    planConstraintsService.coerceRetentionMaxCapacity(
                        repository.retentionMaxCapacity,
                        repository.ownerId,
                        repository.product
                    )
                }
                if (retentionSize != null && retentionSize > 0) {
                    log.info("applying retention for repo ${repository.id} with maxItems=$retentionSize")
                    documentDAO.deleteAllByRepositoryIdAndStatusWithSkip(
                        repository.id,
                        ReleaseStatus.released,
                        retentionSize
                    )
                    documentDAO.deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
                        repository.id,
                        LocalDateTime.now().minusDays(7),
                        ReleaseStatus.unreleased
                    )
                } else {
                    log.debug("no retention with maxItems given repo ${repository.id}")
                }
            }
    }

    @Transactional
    suspend fun applyRetentionStrategy(repositoryId: RepositoryId) {
        val repository = repositoryDAO.findById(repositoryId)!!
        val corrId = coroutineContext.corrId()

        planConstraintsService.coerceRetentionMaxAgeDays(
            repository.retentionMaxAgeDays,
            repository.ownerId,
            repository.product
        )
            ?.let { maxAgeDays ->
                log.info("[$corrId] applying retention with maxAgeDays=$maxAgeDays using field ${repository.retentionMaxAgeDaysReferenceField}")
                val maxDate = LocalDateTime.now().minus(maxAgeDays.toLong(), ChronoUnit.DAYS)
                if (repository.retentionMaxAgeDaysReferenceField == MaxAgeDaysDateField.startingAt) {
                    documentDAO.deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(
                        repository.id,
                        maxDate,
                        ReleaseStatus.released
                    )
                } else {
                    if (repository.retentionMaxAgeDaysReferenceField == MaxAgeDaysDateField.createdAt) {
                        documentDAO.deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
                            repository.id,
                            maxDate,
                            ReleaseStatus.released
                        )
                    } else {
                        documentDAO.deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(
                            repository.id,
                            maxDate,
                            ReleaseStatus.released
                        )
                    }
                }
            } ?: log.debug("[$corrId] no retention with maxAgeDays given")
    }

    @Transactional(propagation = Propagation.REQUIRED)
    suspend fun deleteDocuments(user: User, repositoryId: RepositoryId, documentIds: StringFilter) {
        log.info("[${coroutineContext.corrId()}] deleteDocuments $documentIds")

        val repository = repositoryDAO.findById(repositoryId)!!
        if (repository.ownerId != user.id.uuid) {
            throw PermissionDeniedException("current user ist not owner")
        }

        val documents = documentDAO
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

        documentDAO.deleteAllById(documents.map { it.id });
    }

    @Transactional(readOnly = true)
    suspend fun getRecordFrequency(
        where: RecordsWhereInput,
        groupBy: RecordDateField,
    ): List<RecordFrequency> {
        val query = jpql {
            val whereStatements = prepareWhereStatements(where)
            val dateGroup = expression<Long>("day")

            val groupByEntity = when (groupBy) {
                RecordDateField.createdAt -> path(DocumentEntity::createdAt)
                RecordDateField.publishedAt -> path(DocumentEntity::publishedAt)
                RecordDateField.startingAt -> path(DocumentEntity::startingAt)
            }

            selectNew<Pair<Long, Long>>(
                count(path(DocumentEntity::id)),
                function(Long::class, "fl_trunc_timestamp_as_millis", groupByEntity).`as`(dateGroup)
            ).from(
                entity(DocumentEntity::class),
            )
                .whereAnd(
                    groupByEntity.isNotNull(),
                    path(DocumentEntity::repositoryId).eq(UUID.fromString(where.repository.id)),
                    path(DocumentEntity::publishedAt).lt(LocalDateTime.now()),
                    *whereStatements.toTypedArray()
                )
                .groupBy(dateGroup)
        }

        val context = JpqlRenderContext()
        return withContext(Dispatchers.IO) {
            val q = entityManager.createQuery(query, context)
            q.resultList.map { pair -> RecordFrequency(pair.first.toInt(), pair.second) }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    suspend fun processDocumentPlugins(documentId: DocumentId, jobs: List<DocumentPipelineJob>): Document? {
        val corrId = coroutineContext.corrId()
        log.debug("[$corrId] ${jobs.size} processPlugins for document $documentId")
        val document = documentDAO.findByIdWithSource(documentId)!!
        val logCollector = LogCollector()

        return document?.let {
            try {
                val repository = repositoryDAO.findById(document.repositoryId)!!

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
                                when (val plugin = pluginService.resolveById<FeedlessPlugin>(job.pluginId)) {
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
                                        log.info("[$corrId] ignoring ${plugin.id()} plugin")
                                        state.currentDocument
                                    }

                                    else -> {
                                        if (plugin == null) {
                                            log.error(
                                                "[$corrId] Invalid pluginId '${job.pluginId}'. Available: [${
                                                    pluginService.findAll().joinToString(", ") { "'${it.id()}'" }
                                                }]")
                                        } else {
                                            log.warn("[$corrId] resolved unsupported plugin ${plugin}")
                                        }
                                        throw IllegalArgumentException("Invalid plugin")
                                    }
                                }

                            log.debug("[$corrId] executed ${job.pluginId} for $documentId")
                            documentPipelineJobDAO.save(job.copy(status = PipelineJobStatus.SUCCEEDED))
                            ProcessingState(updatedDocument, true, false)
                        } catch (e: Exception) {
                            if (e is ResumableHarvestException || e is TooManyConnectionsPerHostException) {
                                delayJob(job, e, state.currentDocument)
                            } else {
                                if (e !is FilterMismatchException) {
                                    log.warn("[$corrId] ${e::class.simpleName} ${e.message}")
                                } else {
                                    log.info("[$corrId] ${e::class.simpleName} ${e.message}")
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
                log.warn("[$corrId] aborting pipeline for document, cause ${throwable.message}")
                deleteDocument(document)
                null
            }
        }
    }

    private suspend fun releaseDocument(
        document: Document,
        repository: Repository,
    ) {
//    forwardToMail(corrId, document, repository)
        log.info("[${coroutineContext.corrId()}] releasing document ${document.id}")

        documentDAO.save(
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
        if (repository.pushNotificationsEnabled) {
            telegramBotServiceMaybe.getOrNull()?.let { telegramBot ->
                telegramBot.findByUserIdAndAuthorizedIsTrue(repository.ownerId)?.let { telegramLink ->
                    documents.forEach {
                        messageService.publishMessage(
                            TelegramBotService.toTopic(telegramLink.chatId),
                            it.asJsonItem(repository)
                        )
                    }
                }
            }
        }
    }

    private suspend fun deleteDocument(document: Document) {
        log.info("[${coroutineContext.corrId()}] delete document ${document.id}")
        documentDAO.deleteById(document.id)
    }

    private suspend fun delayJob(
        job: DocumentPipelineJob,
        e: Exception,
        document: Document,
    ) {
        log.info("[${coroutineContext.corrId()}] delaying ${job.documentId} (${job.pluginId})")

        documentPipelineJobDAO.save(
            job.copy(
                coolDownUntil = if (e is ResumableHarvestException) {
                    LocalDateTime.now().plus(e.nextRetryAfter)
                } else {
                    LocalDateTime.now().plusSeconds(120)
                }
            )
        )
        documentDAO.save(document) // todo still needed?
    }

    @Transactional(readOnly = true)
    suspend fun countByRepositoryId(repositoryId: RepositoryId): Long {
        return documentDAO.countByRepositoryId(repositoryId)
    }

    @Transactional
    suspend fun createDocument(data: CreateRecordInput): Document {
        val repositoryId = RepositoryId(data.repositoryId.id)

        val repository = repositoryDAO.findById(repositoryId)!!

        permissionService.canWrite(repository)

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

        return documentDAO.save(document)
    }

    @Transactional
    suspend fun updateDocument(data: RecordUpdateInput, id: DocumentId): Document {
        var document = documentDAO.findById(id)!!
            .copy(
                updatedAt = LocalDateTime.now()
            )

        permissionService.canWrite(document)

        document = data.url?.let {
            document.copy(url = it.set)
        } ?: document

        document = data.text?.let {
            document.copy(text = it.set)
        } ?: document

        document = data.title?.let {
            document.copy(title = it.set)
        } ?: document

        return documentDAO.save(document)
    }

    @Transactional
    suspend fun deleteById(documentId: DocumentId) {
        documentDAO.deleteById(documentId)
    }

    @Transactional(readOnly = true)
    suspend fun findFirstByContentHashOrUrlAndRepositoryId(
        contentHash: String,
        url: String,
        repositoryId: RepositoryId
    ): Document? {
        return documentDAO.findFirstByContentHashOrUrlAndRepositoryId(contentHash, url, repositoryId)
    }

    @Transactional
    suspend fun saveAll(documents: List<Document>): List<Document> {
        return documentDAO.saveAll(documents)
    }

    @Transactional(readOnly = true)
    suspend fun findAllBySourceId(sourceId: SourceId, pageable: PageableRequest): List<Document> {
        return documentDAO.findAllBySourceId(sourceId, pageable)
    }

//  private suspend fun forwardToMail(document: DocumentEntity, repository: RepositoryEntity) {
//    val mailForwards = withContext(Dispatchers.IO) {
//      mailForwardDAO.findAllByRepositoryId(repository.id)
//    }
//    if (mailForwards.isNotEmpty()) {
//      val authorizedMailForwards =
//        mailForwards.filterTo(ArrayList()) { it: MailForwardEntity -> it.authorized }.map { it.email }
//      if (authorizedMailForwards.isEmpty()) {
//        log.warn("[$corrId] no authorized mail-forwards available of ${mailForwards.size}")
//      } else {
//        val (mailFormatter, params) = pluginService.resolveMailFormatter(repository)
//        log.debug("[$corrId] using formatter ${mailFormatter::class.java.name}")
//
//        val from = mailService.getNoReplyAddress(repository.product)
//        val to = authorizedMailForwards.toTypedArray()
//
//        log.debug("[$corrId] resolved mail recipients [${authorizedMailForwards.joinToString(", ")}]")
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


