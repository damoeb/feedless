package org.migor.feedless.document

import com.google.gson.Gson
import jakarta.persistence.EntityManager
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Mother.randomDocumentId
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.Vertical
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.argThat
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.connectedApp.TelegramConnectionEntity
import org.migor.feedless.eq
import org.migor.feedless.generated.types.CreateRecordInput
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.RecordUpdateInput
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput
import org.migor.feedless.message.MessageService
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipeline.plugins.CompositeFieldFilterParams
import org.migor.feedless.pipeline.plugins.CompositeFilterParams
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin
import org.migor.feedless.pipeline.plugins.FulltextPlugin
import org.migor.feedless.pipeline.plugins.FulltextPluginParams
import org.migor.feedless.pipeline.plugins.ItemFilterParams
import org.migor.feedless.pipeline.plugins.StringFilter
import org.migor.feedless.pipeline.plugins.StringFilterOperator
import org.migor.feedless.pipeline.plugins.StringFilterParams
import org.migor.feedless.pipelineJob.DocumentPipelineJob
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.pipelineJob.PipelineJobId
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.toJsonItem
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.session.PermissionService
import org.migor.feedless.session.RequestContext
import org.migor.feedless.transport.TelegramBotService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentServiceTest {

    private lateinit var repository: Repository
    private lateinit var documentRepository: DocumentRepository
    private lateinit var repositoryRepository: RepositoryRepository
    private lateinit var userRepository: UserRepository

    private lateinit var documentService: DocumentService
    private lateinit var propertyService: PropertyService

    private lateinit var currentUser: User
    private lateinit var permissionService: PermissionService
    private lateinit var planConstraintsService: PlanConstraintsService
    private lateinit var pluginService: PluginService
    private lateinit var filterPlugin: CompositeFilterPlugin
    private lateinit var fulltextPlugin: FulltextPlugin
    private lateinit var documentPipelineJobRepository: DocumentPipelineJobRepository
    private lateinit var telegramBotService: TelegramBotService
    private lateinit var messageService: MessageService
    private lateinit var documentId: DocumentId
    private lateinit var repositoryId: RepositoryId
    private lateinit var document: Document

    private val currentUserId = randomUserId()

    @BeforeEach
    fun setUp() = runTest {
        currentUser = mock(User::class.java)
        `when`(currentUser.id).thenReturn(randomUserId())

        userRepository = mock(UserRepository::class.java)
        repositoryRepository = mock(RepositoryRepository::class.java)
        documentRepository = mock(DocumentRepository::class.java)
        permissionService = PermissionService(userRepository, repositoryRepository)
        planConstraintsService = mock(PlanConstraintsService::class.java)
        telegramBotService = mock(TelegramBotService::class.java)
        messageService = mock(MessageService::class.java)
        propertyService = mock(PropertyService::class.java)

        filterPlugin = spy(CompositeFilterPlugin())
        fulltextPlugin = mock(FulltextPlugin::class.java)
        `when`(fulltextPlugin.id()).thenReturn(FeedlessPlugins.org_feedless_fulltext.name)
        pluginService = PluginService(
            entityPlugins = emptyList(),
            transformerPlugins = emptyList(),
            plugins = listOf(filterPlugin, fulltextPlugin)
        )
//    `when`(pluginService.resolveById<FilterEntityPlugin>(eq(FeedlessPlugins.org_feedless_filter.name))).thenReturn(filterPlugin)
        documentPipelineJobRepository = mock(DocumentPipelineJobRepository::class.java)

        documentService = DocumentService(
            documentRepository,
            mock(EntityManager::class.java),
            repositoryRepository,
            planConstraintsService,
            documentPipelineJobRepository,
            pluginService,
            permissionService,
            Optional.of(telegramBotService),
            messageService,
            propertyService,
        )

        documentId = randomDocumentId()
        repositoryId = RepositoryId()
        document = Document(
            id = documentId,
            url = "http://localhost",
            title = "foo",
            text = "foo bar",
            status = ReleaseStatus.unreleased,
            publishedAt = LocalDateTime.now(),
            repositoryId = repositoryId,
            contentHash = ""
        )

        `when`(propertyService.apiGatewayUrl).thenReturn("http://foo.bar")

        assertThat(document.toJsonItem(propertyService, EntityVisibility.isPublic)).isNotNull();

        repository = Repository(
            id = repositoryId,
            title = "",
            ownerId = UserId(),
            visibility = EntityVisibility.isPublic,
            lastUpdatedAt = LocalDateTime.now(),
            retentionMaxAgeDaysReferenceField = MaxAgeDaysDateField.publishedAt,
        )
    }

    private suspend fun mockDocumentFindById(id: DocumentId, document: Document) {
        `when`(documentRepository.findByIdWithSource(eq(id))).thenReturn(document)
    }

    private suspend fun mockRepositoryFindById(id: RepositoryId, repository: Repository) {
        `when`(repositoryRepository.findById(eq(id))).thenReturn(repository)
    }

    @Test
    fun `processDocumentPlugins will remove documents when dropped by filter`() = runTest {
        val filterJob = DocumentPipelineJob(
            pluginId = FeedlessPlugins.org_feedless_filter.name,
            sequenceId = 1,
            documentId = DocumentId(),
            executorParams = PluginExecutionJson(
                paramsJsonString = Gson().toJson(
                    listOf(
                        ItemFilterParams(
                            composite = CompositeFilterParams(
                                exclude = CompositeFieldFilterParams(
                                    title = StringFilterParams(
                                        operator = StringFilterOperator.contains,
                                        value = "foo"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        mockDocumentFindById(documentId, document)
        mockRepositoryFindById(repositoryId, repository)

        // when
        documentService.processDocumentPlugins(
            documentId, listOf(
                filterJob
            )
        )

        // then
        verify(filterPlugin).matches(
            any2(), any2(), any(Int::class.java)
        )
        verify(documentRepository).deleteById(eq(documentId))
    }

    @Test
    fun `processDocumentPlugins will save document when not dropped by filter`() = runTest {
        val filterJob = DocumentPipelineJob(
            pluginId = FeedlessPlugins.org_feedless_filter.name,
            sequenceId = 1,
            documentId = DocumentId(),
            executorParams = PluginExecutionJson(
                paramsJsonString = Gson().toJson(
                    listOf(
                        ItemFilterParams(
                            composite = CompositeFilterParams(
                                exclude = CompositeFieldFilterParams(
                                    title = StringFilterParams(
                                        operator = StringFilterOperator.contains,
                                        value = "foo2"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        mockDocumentFindById(documentId, document)
        mockRepositoryFindById(repositoryId, repository)

        // when
        documentService.processDocumentPlugins(
            documentId, listOf(
                filterJob
            )
        )

        // then
        verify(documentRepository).save(argThat { it.id == documentId })
    }

    @Test
    fun `processDocumentPlugins will map document`() = runTest {
        val mapJob = DocumentPipelineJob(
            pluginId = FeedlessPlugins.org_feedless_fulltext.name,
            sequenceId = 1,
            documentId = DocumentId(),
            executorParams = PluginExecutionJson(
                paramsJsonString = Gson().toJson(
                    FulltextPluginParams(
                        summary = true,
                        readability = true,
                        inheritParams = false
                    )
                )
            )
        )

        `when`(
            fulltextPlugin.mapEntity(
                any(Document::class.java),
                any(Repository::class.java),
                any(String::class.java),
                any(LogCollector::class.java),
            )
        ).thenAnswer {
            val d = it.arguments[0] as Document
            d.copy(title = "mapped to a different title")
        }

        mockDocumentFindById(documentId, document)
        mockRepositoryFindById(repositoryId, repository)

        // when
        documentService.processDocumentPlugins(
            documentId, listOf(
                mapJob
            )
        )

        // then
        verify(documentRepository).save(argThat { it.id == document.id })
        verify(documentRepository).save(argThat { it.title == "mapped to a different title" })
    }

    @Test
    fun `released document will be forwarded to telegram, if repository allow notifications`() = runTest {
        mockDocumentFindById(documentId, document.copy(status = ReleaseStatus.unreleased))
        mockRepositoryFindById(repositoryId, repository.copy(pushNotificationsEnabled = true))

        val telegramConnection = mock(TelegramConnectionEntity::class.java)
        `when`(telegramConnection.chatId).thenReturn(12345)
        `when`(telegramBotService.findByUserIdAndAuthorizedIsTrue(any2())).thenReturn(telegramConnection)

        // when
        documentService.processDocumentPlugins(
            documentId, listOf()
        )

        // then
        verify(documentRepository).save(argThat { it.status == ReleaseStatus.released })
        verify(documentRepository).save(argThat { it.id == documentId })
        verify(messageService).publishMessage(any2(), any2())
    }

    @Test
    fun `released document won't be forwarded to telegram, if repository disabled notifications`() = runTest {
        mockDocumentFindById(documentId, document.copy(status = ReleaseStatus.unreleased))
        mockRepositoryFindById(repositoryId, repository.copy(pushNotificationsEnabled = false))

        val telegramConnection = mock(TelegramConnectionEntity::class.java)
        `when`(telegramConnection.chatId).thenReturn(12345)
        `when`(telegramBotService.findByUserIdAndAuthorizedIsTrue(any2())).thenReturn(telegramConnection)

        // when
        documentService.processDocumentPlugins(
            documentId, listOf()
        )

        // then
        verify(documentRepository).save(argThat { it.status == ReleaseStatus.released })

        verify(messageService, times(0)).publishMessage(any2(), any2())
    }


    @Test
    fun `processDocumentPlugins will release document when all plugins are executed`() = runTest {
        // given
        mockDocumentFindById(documentId, document.copy(status = ReleaseStatus.unreleased))
        mockRepositoryFindById(repositoryId, repository)

        // when
        documentService.processDocumentPlugins(
            documentId, listOf()
        )

        // then
        verify(documentRepository).save(argThat { it.status == ReleaseStatus.released })
        verify(documentRepository).save(argThat { it.id == documentId })
    }

    @Test
    @Disabled
    fun `processDocumentPlugins will save document when execution gets delayed`() =
        runTest(context = RequestContext(userId = currentUserId)) {
            val jobId = PipelineJobId()
            val job = DocumentPipelineJob(
                id = jobId,
                sequenceId = 1,
                documentId = documentId,
                pluginId = FeedlessPlugins.org_feedless_fulltext.name,
                executorParams = PluginExecutionJson()
            )
            assertThat(job.coolDownUntil).isNull()

            `when`(
                fulltextPlugin.mapEntity(
                    any(Document::class.java),
                    any(Repository::class.java),
                    any(String::class.java),
                    any(LogCollector::class.java),
                )
            ).thenThrow(ResumableHarvestException("foo", Duration.ofMinutes(2)))

            mockDocumentFindById(documentId, document)
            mockRepositoryFindById(repositoryId, repository)

            var savedDocument: Document? = null
            `when`(documentRepository.save(any(Document::class.java))).thenAnswer {
                savedDocument = it.arguments[0] as Document
                savedDocument
            }

            var savedJob: DocumentPipelineJob? = null
            `when`(documentPipelineJobRepository.save(any(DocumentPipelineJob::class.java))).thenAnswer {
                savedJob = it.arguments[0] as DocumentPipelineJob
                savedJob
            }

            // when
            documentService.processDocumentPlugins(
                documentId, listOf(job)
            )

            // then
            assertThat(savedJob).isNotNull
            assertThat(savedJob!!.id).isEqualTo(jobId)
            assertThat(savedJob!!.coolDownUntil).isNotNull()
            assertThat(savedDocument).isNotNull
            assertThat(savedDocument!!.id).isEqualTo(documentId)
        }

    @Test
    fun `applyRetentionStrategy by startingAt`() = runTest(context = RequestContext(userId = currentUserId)) {
        val repositoryId = randomRepositoryId()
        val repository =
            mockRepository(repositoryId, currentUserId, maxAgeDaysDateField = MaxAgeDaysDateField.startingAt)

        mockDocumentFindById(documentId, document)
        mockRepositoryFindById(repositoryId, repository)

        // when
        documentService.applyRetentionStrategy(repositoryId)

        // then
        verify(documentRepository).deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(
            eq(repositoryId), any(LocalDateTime::class.java), any(
                ReleaseStatus::class.java
            )
        )
    }

    @Test
    fun `applyRetentionStrategy by createdAt`() = runTest(context = RequestContext(userId = currentUserId)) {
        val repositoryId = randomRepositoryId()
        val repository =
            mockRepository(repositoryId, currentUserId, maxAgeDaysDateField = MaxAgeDaysDateField.createdAt)

        mockDocumentFindById(documentId, document)
        mockRepositoryFindById(repositoryId, repository)

        // when
        documentService.applyRetentionStrategy(repositoryId)

        // then
        verify(documentRepository).deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
            eq(repositoryId), any(LocalDateTime::class.java), any(
                ReleaseStatus::class.java
            )
        )
    }

    @Test
    fun `applyRetentionStrategy by publishedAt`() = runTest(context = RequestContext(userId = currentUserId)) {
        val repositoryId = randomRepositoryId()
        val repository =
            mockRepository(repositoryId, currentUserId, maxAgeDaysDateField = MaxAgeDaysDateField.publishedAt)
        mockDocumentFindById(documentId, document)
        mockRepositoryFindById(repositoryId, repository)

        // when
        documentService.applyRetentionStrategy(repositoryId)

        // then
        verify(documentRepository).deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(
            eq(repositoryId), any(LocalDateTime::class.java), any(
                ReleaseStatus::class.java
            )
        )
    }

    @Test
    fun `create document without permissions fails`() {
        val documentId = randomDocumentId()
        val repositoryId = randomRepositoryId()

        assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
            runTest(context = RequestContext(userId = currentUserId)) {
                mockUser(currentUserId)
                mockDocument(documentId = documentId, repositoryId = repositoryId)
                mockRepository(repositoryId, ownerId = randomUserId())

                mockDocumentFindById(documentId, document.copy(status = ReleaseStatus.unreleased))
                mockRepositoryFindById(repositoryId, repository.copy(pushNotificationsEnabled = true))

                val data = CreateRecordInput(
                    title = "foo",
                    publishedAt = Date().time,
                    url = "",
                    text = "",
                    repositoryId = RepositoryUniqueWhereInput(id = repositoryId.uuid.toString()),
                )
                documentService.createDocument(data)
            }
        }
    }

    @Test
    fun `create document of owner works`() = runTest(context = RequestContext(userId = currentUserId)) {
        val repositoryId = randomRepositoryId()

        mockUser(currentUserId)
        `when`(documentRepository.save(any(Document::class.java))).thenAnswer { it.arguments[0] }
        mockRepository(repositoryId, ownerId = currentUserId)

        val data = CreateRecordInput(
            title = "foo",
            publishedAt = Date().time,
            url = "",
            text = "",
            repositoryId = RepositoryUniqueWhereInput(id = repositoryId.uuid.toString()),
        )
        documentService.createDocument(data)

        verify(documentRepository).save(any(Document::class.java))
    }

    @Test
    fun `update document without permissions fails`() = runTest {
        val documentId = randomDocumentId()
        val repositoryId = randomRepositoryId()

        mockUser(currentUserId)
        mockDocument(documentId = documentId, repositoryId = repositoryId)

        assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
            runTest(context = RequestContext(userId = currentUserId)) {
                mockRepository(repositoryId, ownerId = randomUserId())

                val data = RecordUpdateInput()
                val where = DocumentId(documentId.uuid)
                documentService.updateDocument(data, where)
            }
        }
    }

    @Test
    fun `update document of owner works`() = runTest(context = RequestContext(userId = currentUserId)) {
        // given
        val documentId = randomDocumentId()
        val repositoryId = randomRepositoryId()

        mockUser(currentUserId)
        val document = mockDocument(documentId = documentId, repositoryId = repositoryId)
        val repository = mockRepository(repositoryId, ownerId = currentUserId)
        `when`(documentRepository.save(any(Document::class.java))).thenAnswer { it.arguments[0] }

        mockDocumentFindById(documentId, document)
        mockRepositoryFindById(repositoryId, repository)


        val data = RecordUpdateInput()
        val where = DocumentId(documentId.uuid)

        // when
        documentService.updateDocument(data, where)

        // then

//        val captor = ArgumentCaptor.forClass(Document::class.java)
        verify(documentRepository).save(any2())
//        val savedDocument = captor.value
//        assertThat(savedDocument.id).isEqualTo(documentId)
//        assertThat(savedDocument.description).isEqualTo("new-description")
//        assertThat(savedDocument.sourcesSyncCron).isEqualTo("* * * * * *")
    }


    @Test
    fun `given deleteDocuments is executed not by the owner, it fails`() {
        val repository = mock(Repository::class.java)
        val repositoryId = randomRepositoryId()
        `when`(repository.id).thenReturn(repositoryId)

        `when`(repository.ownerId).thenReturn(UserId())

        assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
            runTest {
                `when`(repositoryRepository.findById(any2())).thenReturn(repository)
                documentService.deleteDocuments(currentUser, repositoryId, StringFilter())
            }
        }
    }

    private suspend fun mockUser(userId: UserId): User {
        val user = mock(User::class.java)
        `when`(user.id).thenReturn(userId)
        `when`(userRepository.findById(eq(userId))).thenReturn(user)
        return user
    }

    private suspend fun mockDocument(documentId: DocumentId, repositoryId: RepositoryId): Document {
        val document = Document(
            id = documentId,
            url = "http://localhost",
            title = "foo",
            text = "foo bar",
            status = ReleaseStatus.unreleased,
            publishedAt = LocalDateTime.now(),
            repositoryId = repositoryId,
            contentHash = ""
        )

        `when`(documentRepository.findById(eq(documentId))).thenReturn(document)

        return document
    }

    private suspend fun mockRepository(
        repositoryId: RepositoryId,
        ownerId: UserId,
        maxAgeDaysDateField: MaxAgeDaysDateField? = null,
        retentionMaxCapacity: Int? = null
    ): Repository {
        val repository = mock(Repository::class.java)
        `when`(repository.id).thenReturn(repositoryId)
        `when`(repository.ownerId).thenReturn(ownerId)
        maxAgeDaysDateField?.let {
            `when`(repository.retentionMaxAgeDaysReferenceField).thenReturn(it)
        }
        retentionMaxCapacity?.let {
            `when`(repository.retentionMaxCapacity).thenReturn(it)
        }
        `when`(repository.retentionMaxAgeDays).thenReturn(20)
        `when`(repository.id).thenReturn(repositoryId)
        `when`(repository.product).thenReturn(Vertical.feedless)
        `when`(repositoryRepository.findById(eq(repositoryId))).thenReturn(repository)

        `when`(
            planConstraintsService.coerceRetentionMaxAgeDays(
                repository.retentionMaxAgeDays,
                repository.ownerId,
                repository.product
            )
        ).thenReturn(20)

        `when`(
            planConstraintsService.coerceRetentionMaxCapacity(
                repository.retentionMaxCapacity,
                repository.ownerId,
                repository.product
            )
        ).thenReturn(retentionMaxCapacity)

        return repository
    }
}
