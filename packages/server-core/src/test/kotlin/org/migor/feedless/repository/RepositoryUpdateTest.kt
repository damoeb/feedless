package org.migor.feedless.repository

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.Vertical
import org.migor.feedless.any2
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentService
import org.migor.feedless.eq
import org.migor.feedless.generated.types.BoolUpdateOperationsInput
import org.migor.feedless.generated.types.NullableIntUpdateOperationsInput
import org.migor.feedless.generated.types.NullableLongUpdateOperationsInput
import org.migor.feedless.generated.types.NullableStringUpdateOperationsInput
import org.migor.feedless.generated.types.RecordDateField
import org.migor.feedless.generated.types.RecordDateFieldUpdateOperationsInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.RetentionUpdateInput
import org.migor.feedless.generated.types.SourcesUpdateInput
import org.migor.feedless.generated.types.StringUpdateOperationsInput
import org.migor.feedless.generated.types.Visibility
import org.migor.feedless.generated.types.VisibilityUpdateOperationsInput
import org.migor.feedless.data.jpa.repository.RepositoryDAO
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.session.RequestContext
import org.migor.feedless.session.SessionService
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserService
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationContext
import java.time.LocalDateTime
import java.util.*


class RepositoryUpdateTest {

    private lateinit var repositoryService: RepositoryService
    private lateinit var repositoryDAO: RepositoryDAO
    private lateinit var sessionService: SessionService
    private lateinit var planConstraintsService: PlanConstraintsService
    private lateinit var repositoryId: RepositoryId
    private lateinit var ownerId: UserId
    private lateinit var repository: RepositoryEntity
    private lateinit var data: RepositoryUpdateDataInput
    private lateinit var applicationContext: ApplicationContext
    private lateinit var sourceService: SourceService
    private val currentUserId = randomUserId()

    @BeforeEach
    fun setUp() {
        repositoryDAO = mock(RepositoryDAO::class.java)
        sessionService = mock(SessionService::class.java)
        planConstraintsService = mock(PlanConstraintsService::class.java)
        applicationContext = mock(ApplicationContext::class.java)
        sourceService = mock(SourceService::class.java)

        repositoryService = spy(
            RepositoryService(
//      mock(UserDAO::class.java),
                repositoryDAO,
                sessionService,
                mock(UserService::class.java),
                planConstraintsService,
                mock(DocumentService::class.java),
                mock(PropertyService::class.java),
                sourceService,
                applicationContext,
            )
        )

        `when`(applicationContext.getBean(eq(RepositoryService::class.java))).thenReturn(repositoryService)

        repositoryId = randomRepositoryId()
        ownerId = randomUserId()
        repository = mock(RepositoryEntity::class.java)
        `when`(repository.id).thenReturn(repositoryId.value)
        `when`(repository.ownerId).thenReturn(ownerId.value)
        `when`(repository.lastUpdatedAt).thenReturn(LocalDateTime.now())
        data = RepositoryUpdateDataInput(
            description = NullableStringUpdateOperationsInput(set = "new-description"),
            refreshCron = NullableStringUpdateOperationsInput(set = "* * * * * *"),
            title = StringUpdateOperationsInput(set = "new-title"),
            pushNotificationsMuted = BoolUpdateOperationsInput(set = true),
            visibility = VisibilityUpdateOperationsInput(set = Visibility.isPrivate),
            retention = RetentionUpdateInput(
                maxCapacity = NullableIntUpdateOperationsInput(),
                maxAgeDays = NullableIntUpdateOperationsInput(),
                ageReferenceField = RecordDateFieldUpdateOperationsInput(
                    set = RecordDateField.createdAt
                )
            ),
            plugins = listOf(),
            nextUpdateAt = NullableLongUpdateOperationsInput(set = 1),
            sources = SourcesUpdateInput(
                remove = emptyList(),
                update = emptyList(),
                add = emptyList()
            ),
        )
    }

    @Test
    fun `given repo does not exists, update will fail`() {
        assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
            runTest(context = RequestContext(userId = currentUserId)) {
                repositoryService.updateRepository(repositoryId, data)
            }
        }
    }

    @Test
    fun `given current user has insuffieient priveleges, update will fail`() {
        assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
            runTest(context = RequestContext(userId = randomUserId())) {
                doReturn(Optional.of(repository)).`when`(repositoryService).findById(any2())
                repositoryService.updateRepository(repositoryId, data)
            }
        }
    }

    @Test
    fun `given all requirements are met, repository data will be updated`() =
        runTest(context = RequestContext(userId = ownerId)) {
            `when`(planConstraintsService.auditCronExpression(any2())).thenAnswer {
                it.arguments[0]
            }
            `when`(
                planConstraintsService.coerceMinScheduledNextAt(
                    any2(),
                    any2(),
                    any2(),
                    any2(),
                )
            ).thenReturn(LocalDateTime.now())
            mockActiveProductFromRequest()
            `when`(repositoryDAO.findById(any2())).thenReturn(Optional.of(repository))
            mockRepositorySave()

            repositoryService.updateRepository(repositoryId, data)

            verify(repository).title = "new-title"
            verify(repository).description = "new-description"
            verify(repository).sourcesSyncCron = "* * * * * *"
//    verify(repository).pushNotificationsMuted = true
//    verify(repository).visibility = EntityVisibility.isPrivate
//    verify(repository).plugins = emptyList()
//    verify(repository).triggerScheduledNextAt = any(LocalDateTime::class.java)
//    verify(repository).retentionMaxCapacity = 0
//    verify(repository).retentionMaxAgeDays = 0
//    verify(repository).retentionMaxAgeDaysReferenceField = MaxAgeDaysDateField.startingAt
//    verify(repository).sources = mutableListOf()
//
            verify(repositoryDAO).save(any2())
        }

    @Test
    fun `given a valid update request, sources can be added`() = runTest(context = RequestContext(userId = ownerId)) {
        // given
        doReturn(Optional.of(repository)).`when`(repositoryService).findById(any2())
        mockActiveProductFromRequest()
        mockRepositorySave()

        // when
        repositoryService.updateRepository(repositoryId, data)

        // then
        verify(sourceService).createSources(any2(), any2(), any2())
    }

    @Test
    fun `given a valid update request, sources can be updated`() = runTest(context = RequestContext(userId = ownerId)) {
        // given
        doReturn(Optional.of(repository)).`when`(repositoryService).findById(any2())
        mockActiveProductFromRequest()
        mockRepositorySave()

        // when
        repositoryService.updateRepository(repositoryId, data)

        // then
        verify(sourceService).updateSources(any2(), any2())
    }

    @Test
    fun `given a valid update request, sources can be removed`() = runTest(context = RequestContext(userId = ownerId)) {
        // given
        doReturn(Optional.of(repository)).`when`(repositoryService).findById(any2())
        mockActiveProductFromRequest()
        mockRepositorySave()

        // when
        repositoryService.updateRepository(repositoryId, data)

        // then
        verify(sourceService).deleteAllById(any2(), any2())
    }

    private fun mockRepositorySave() {
        `when`(repositoryDAO.save(any2())).thenAnswer { it.arguments[0] }
    }

    private suspend fun mockActiveProductFromRequest() {
        `when`(sessionService.activeProductFromRequest()).thenReturn(Vertical.rssProxy)
    }

}
