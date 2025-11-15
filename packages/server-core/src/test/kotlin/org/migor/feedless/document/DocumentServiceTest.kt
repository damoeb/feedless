package org.migor.feedless.document

import jakarta.persistence.EntityManager
import kotlinx.coroutines.test.runTest
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.Mother.randomDocumentId
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.Vertical
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.eq
import org.migor.feedless.generated.types.CompositeFieldFilterParamsInput
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.CreateRecordInput
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.FulltextPluginParamsInput
import org.migor.feedless.generated.types.ItemFilterParamsInput
import org.migor.feedless.generated.types.RecordUniqueWhereInput
import org.migor.feedless.generated.types.RecordUpdateInput
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput
import org.migor.feedless.generated.types.StringFilterInput
import org.migor.feedless.generated.types.StringFilterOperator
import org.migor.feedless.generated.types.StringFilterParamsInput
import org.migor.feedless.jpa.document.DocumentDAO
import org.migor.feedless.jpa.document.DocumentEntity
import org.migor.feedless.jpa.documentPipelineJob.DocumentPipelineJobDAO
import org.migor.feedless.jpa.documentPipelineJob.DocumentPipelineJobEntity
import org.migor.feedless.jpa.repository.MaxAgeDaysDateField
import org.migor.feedless.jpa.repository.RepositoryDAO
import org.migor.feedless.jpa.repository.RepositoryEntity
import org.migor.feedless.jpa.source.actions.PluginExecutionJsonEntity
import org.migor.feedless.jpa.user.TelegramConnectionEntity
import org.migor.feedless.jpa.user.UserDAO
import org.migor.feedless.jpa.user.UserEntity
import org.migor.feedless.message.MessageService
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin
import org.migor.feedless.pipeline.plugins.FulltextPlugin
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.session.PermissionService
import org.migor.feedless.session.RequestContext
import org.migor.feedless.transport.TelegramBotService
import org.migor.feedless.user.UserId
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

  private lateinit var repository: RepositoryEntity
  private lateinit var documentDAO: DocumentDAO
  private lateinit var repositoryDAO: RepositoryDAO
  private lateinit var userDAO: UserDAO

  private lateinit var documentService: DocumentService

  private lateinit var currentUser: UserEntity
  private lateinit var permissionService: PermissionService
  private lateinit var planConstraintsService: PlanConstraintsService
  private lateinit var pluginService: PluginService
  private lateinit var filterPlugin: CompositeFilterPlugin
  private lateinit var fulltextPlugin: FulltextPlugin
  private lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO
  private lateinit var telegranBotService: TelegramBotService
  private lateinit var messageService: MessageService
  private lateinit var documentId: DocumentId
  private lateinit var document: DocumentEntity

  private val currentUserId = randomUserId()

  @BeforeEach
  fun setUp() {
    currentUser = mock(UserEntity::class.java)
    `when`(currentUser.id).thenReturn(UUID.randomUUID())

    userDAO = mock(UserDAO::class.java)
    repositoryDAO = mock(RepositoryDAO::class.java)
    documentDAO = mock(DocumentDAO::class.java)
    permissionService = PermissionService(userDAO, repositoryDAO)
    planConstraintsService = mock(PlanConstraintsService::class.java)
    telegranBotService = mock(TelegramBotService::class.java)
    messageService = mock(MessageService::class.java)

    filterPlugin = spy(CompositeFilterPlugin())
    fulltextPlugin = mock(FulltextPlugin::class.java)
    `when`(fulltextPlugin.id()).thenReturn(FeedlessPlugins.org_feedless_fulltext.name)
    pluginService = PluginService(
      entityPlugins = emptyList(),
      transformerPlugins = emptyList(),
      plugins = listOf(filterPlugin, fulltextPlugin)
    )
//    `when`(pluginService.resolveById<FilterEntityPlugin>(eq(FeedlessPlugins.org_feedless_filter.name))).thenReturn(filterPlugin)
    documentPipelineJobDAO = mock(DocumentPipelineJobDAO::class.java)

    documentService = DocumentService(
      documentDAO,
      mock(EntityManager::class.java),
      repositoryDAO,
      planConstraintsService,
      documentPipelineJobDAO,
      pluginService,
      permissionService,
      Optional.of(telegranBotService),
      messageService
    )

    documentId = randomDocumentId()
    val repositoryId = UUID.randomUUID()
    document = mock(DocumentEntity::class.java)
    `when`(document.id).thenReturn(documentId.value)
    `when`(document.url).thenReturn("http://localhost")
    `when`(document.title).thenReturn("foo bar")
    `when`(document.status).thenReturn(ReleaseStatus.unreleased)
    `when`(document.publishedAt).thenReturn(LocalDateTime.now())
    `when`(document.repositoryId).thenReturn(repositoryId)
    `when`(documentDAO.findByIdWithSource(eq(documentId.value))).thenReturn(document)

    repository = mock(RepositoryEntity::class.java)
    `when`(repository.ownerId).thenReturn(UUID.randomUUID())
    `when`(repositoryDAO.findById(eq(repositoryId))).thenReturn(Optional.of(repository))
  }

  @Test
  fun `processDocumentPlugins will remove documents when dropped by filter`() = runTest {
    val filterJob = DocumentPipelineJobEntity()
    filterJob.pluginId = FeedlessPlugins.org_feedless_filter.name
    filterJob.executorParams = PluginExecutionJsonEntity(
      org_feedless_filter = listOf(
        ItemFilterParamsInput(
          composite = CompositeFilterParamsInput(
            exclude = CompositeFieldFilterParamsInput(
              title = StringFilterParamsInput(
                operator = StringFilterOperator.contains,
                value = "foo"
              )
            )
          )
        )
      )
    )
    documentService.processDocumentPlugins(
      documentId, listOf(
        filterJob
      )
    )
    verify(filterPlugin).matches(
      any2(), any2(), any(Int::class.java)
    )
    verify(documentDAO).delete(eq(document))
  }

  @Test
  fun `processDocumentPlugins will save document when not dropped by filter`() = runTest {
    val filterJob = DocumentPipelineJobEntity()
    filterJob.pluginId = FeedlessPlugins.org_feedless_filter.name
    filterJob.executorParams = PluginExecutionJsonEntity(
      org_feedless_filter = listOf(
        ItemFilterParamsInput(
          composite = CompositeFilterParamsInput(
            exclude = CompositeFieldFilterParamsInput(
              title = StringFilterParamsInput(
                operator = StringFilterOperator.contains,
                value = "foo2"
              )
            )
          )
        )
      )
    )
    documentService.processDocumentPlugins(
      documentId, listOf(
        filterJob
      )
    )
    verify(documentDAO).save(eq(document))
  }

  @Test
  fun `processDocumentPlugins will map document`() = runTest {
    val mapJob = DocumentPipelineJobEntity()
    mapJob.pluginId = FeedlessPlugins.org_feedless_fulltext.name
    mapJob.executorParams = PluginExecutionJsonEntity(
      org_feedless_fulltext = FulltextPluginParamsInput(
        summary = true,
        readability = true,
        inheritParams = false
      )
    )
    `when`(
      fulltextPlugin.mapEntity(
        eq(document),
        any2(),
        any2(),
        any2(),
      )
    ).thenAnswer {
      val d = it.arguments[0] as DocumentEntity
      d.title = StringUtils.reverse(d.title)
      d
    }

    documentService.processDocumentPlugins(
      documentId, listOf(
        mapJob
      )
    )
    verify(document).title = "rab oof"
    verify(documentDAO).save(eq(document))
  }

  @Test
  fun `released document will be forwarded to telegram, if repository allow notifications`() = runTest {
    assertThat(document.status).isEqualTo(ReleaseStatus.unreleased)
    `when`(repository.pushNotificationsEnabled).thenReturn(true)

    val telegramConnection = mock(TelegramConnectionEntity::class.java)
    `when`(telegramConnection.chatId).thenReturn(12345)
    `when`(telegranBotService.findByUserIdAndAuthorizedIsTrue(any2())).thenReturn(telegramConnection)

    // when
    documentService.processDocumentPlugins(
      documentId, listOf()
    )

    verify(document).status = ReleaseStatus.released
    verify(messageService).publishMessage(any2(), any2())
  }

  @Test
  fun `released document won't be forwarded to telegram, if repository disabled notifications`() = runTest {
    assertThat(document.status).isEqualTo(ReleaseStatus.unreleased)
    `when`(repository.pushNotificationsEnabled).thenReturn(false)

    val telegramConnection = mock(TelegramConnectionEntity::class.java)
    `when`(telegramConnection.chatId).thenReturn(12345)
    `when`(telegranBotService.findByUserIdAndAuthorizedIsTrue(any2())).thenReturn(telegramConnection)

    // when
    documentService.processDocumentPlugins(
      documentId, listOf()
    )

    verify(document).status = ReleaseStatus.released
    verify(messageService, times(0)).publishMessage(any2(), any2())
  }


  @Test
  fun `processDocumentPlugins will release document when all plugins are executed`() = runTest {
    assertThat(document.status).isEqualTo(ReleaseStatus.unreleased)
    documentService.processDocumentPlugins(
      documentId, listOf()
    )
    verify(document).status = ReleaseStatus.released
    verify(documentDAO).save(eq(document))
  }

  @Test
  fun `processDocumentPlugins will save document when execution gets delayed`() =
    runTest(context = RequestContext(userId = currentUserId)) {
      val job = mock(DocumentPipelineJobEntity::class.java)
      `when`(job.pluginId).thenReturn(FeedlessPlugins.org_feedless_fulltext.name)
      `when`(job.executorParams).thenReturn(PluginExecutionJsonEntity())
      `when`(
        fulltextPlugin.mapEntity(
          any(DocumentEntity::class.java),
          any(RepositoryEntity::class.java),
          any(PluginExecutionJsonEntity::class.java),
          any(LogCollector::class.java),
        )
      ).thenThrow(ResumableHarvestException("foo", Duration.ofMinutes(2)))
      assertThat(job.coolDownUntil).isNull()

      // when
      documentService.processDocumentPlugins(
        documentId, listOf(job)
      )

      // then
      verify(job).coolDownUntil != null
      documentPipelineJobDAO.save(job)
      documentDAO.save(document)
    }

  @Test
  fun `applyRetentionStrategy by startingAt`() = runTest(context = RequestContext(userId = currentUserId)) {
    val repositoryId = randomRepositoryId()
    mockRepository(repositoryId, currentUserId, maxAgeDaysDateField = MaxAgeDaysDateField.startingAt)

    // when
    documentService.applyRetentionStrategy(repositoryId)

    // then
    verify(documentDAO).deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(
      eq(repositoryId.value), any(LocalDateTime::class.java), any(
        ReleaseStatus::class.java
      )
    )
  }

  @Test
  fun `applyRetentionStrategy by createdAt`() = runTest(context = RequestContext(userId = currentUserId)) {
    val repositoryId = randomRepositoryId()
    mockRepository(repositoryId, currentUserId, maxAgeDaysDateField = MaxAgeDaysDateField.createdAt)

    // when
    documentService.applyRetentionStrategy(repositoryId)

    // then
    verify(documentDAO).deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
      eq(repositoryId.value), any(LocalDateTime::class.java), any(
        ReleaseStatus::class.java
      )
    )
  }

  @Test
  fun `applyRetentionStrategy by publishedAt`() = runTest(context = RequestContext(userId = currentUserId)) {
    val repositoryId = randomRepositoryId()
    mockRepository(repositoryId, currentUserId, maxAgeDaysDateField = MaxAgeDaysDateField.publishedAt)

    // when
    documentService.applyRetentionStrategy(repositoryId)

    // then
    verify(documentDAO).deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(
      eq(repositoryId.value), any(LocalDateTime::class.java), any(
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

        val data = CreateRecordInput(
          title = "foo",
          publishedAt = Date().time,
          url = "",
          text = "",
          repositoryId = RepositoryUniqueWhereInput(id = repositoryId.value.toString()),
        )
        documentService.createDocument(data)
      }
    }
  }

  @Test
  fun `create document of owner works`() = runTest(context = RequestContext(userId = currentUserId)) {
    val repositoryId = randomRepositoryId()

    mockUser(currentUserId)
    `when`(documentDAO.save(any(DocumentEntity::class.java))).thenAnswer { it.arguments[0] }
    mockRepository(repositoryId, ownerId = currentUserId)

    val data = CreateRecordInput(
      title = "foo",
      publishedAt = Date().time,
      url = "",
      text = "",
      repositoryId = RepositoryUniqueWhereInput(id = repositoryId.value.toString()),
    )
    documentService.createDocument(data)

    verify(documentDAO).save(any(DocumentEntity::class.java))
  }

  @Test
  fun `update document without permissions fails`() {
    val documentId = randomDocumentId()
    val repositoryId = randomRepositoryId()

    mockUser(currentUserId)
    mockDocument(documentId = documentId, repositoryId = repositoryId)

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        mockRepository(repositoryId, ownerId = randomUserId())

        val data = RecordUpdateInput()
        val where = RecordUniqueWhereInput(id = documentId.value.toString())
        documentService.updateDocument(data, where)
      }
    }
  }

  @Test
  fun `update document of owner works`() = runTest(context = RequestContext(userId = currentUserId)) {
    val documentId = randomDocumentId()
    val repositoryId = randomRepositoryId()

    mockUser(currentUserId)
    val document = mockDocument(documentId = documentId, repositoryId = repositoryId)
    mockRepository(repositoryId, ownerId = currentUserId)
    `when`(documentDAO.save(any(DocumentEntity::class.java))).thenAnswer { it.arguments[0] }

    val data = RecordUpdateInput()
    val where = RecordUniqueWhereInput(id = documentId.value.toString())
    documentService.updateDocument(data, where)

    verify(documentDAO).save(eq(document))
  }


  @Test
  fun `given deleteDocuments is executed not by the owner, it fails`() {
    val repository = mock(RepositoryEntity::class.java)
    val repositoryId = randomRepositoryId()
    `when`(repository.id).thenReturn(repositoryId.value)

    `when`(repository.ownerId).thenReturn(UUID.randomUUID())
    `when`(repositoryDAO.findById(any(UUID::class.java))).thenReturn(Optional.of(repository))

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest {
        documentService.deleteDocuments(currentUser, repositoryId, StringFilterInput())
      }
    }
  }

  private fun mockUser(userId: UserId): UserEntity {
    val user = mock(UserEntity::class.java)
    `when`(user.id).thenReturn(userId.value)
    `when`(userDAO.findById(eq(userId.value))).thenReturn(Optional.of(user))
    return user
  }

  private fun mockDocument(documentId: DocumentId, repositoryId: RepositoryId): DocumentEntity {
    val document = mock(DocumentEntity::class.java)
    `when`(document.id).thenReturn(documentId.value)
    `when`(document.repositoryId).thenReturn(repositoryId.value)
    `when`(documentDAO.findById(eq(documentId.value))).thenReturn(Optional.of(document))
    return document
  }

  private suspend fun mockRepository(
    repositoryId: RepositoryId,
    ownerId: UserId,
    maxAgeDaysDateField: MaxAgeDaysDateField? = null,
    retentionMaxCapacity: Int? = null
  ): RepositoryEntity {
    val repository = mock(RepositoryEntity::class.java)
    `when`(repository.id).thenReturn(repositoryId.value)
    `when`(repository.ownerId).thenReturn(ownerId.value)
    maxAgeDaysDateField?.let {
      `when`(repository.retentionMaxAgeDaysReferenceField).thenReturn(it)
    }
    retentionMaxCapacity?.let {
      `when`(repository.retentionMaxCapacity).thenReturn(it)
    }
    `when`(repository.retentionMaxAgeDays).thenReturn(20)
    `when`(repository.id).thenReturn(repositoryId.value)
    `when`(repository.product).thenReturn(Vertical.feedless)
    `when`(repositoryDAO.findById(eq(repositoryId.value))).thenReturn(Optional.of(repository))

    `when`(
      planConstraintsService.coerceRetentionMaxAgeDays(
        repository.retentionMaxAgeDays,
        UserId(repository.ownerId),
        repository.product
      )
    ).thenReturn(20)

    `when`(
      planConstraintsService.coerceRetentionMaxCapacity(
        repository.retentionMaxCapacity,
        UserId(repository.ownerId),
        repository.product
      )
    ).thenReturn(retentionMaxCapacity)

    return repository
  }
}
