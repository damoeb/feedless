package org.migor.feedless.repository

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomSourceId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.api.fromDto
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.repository.RepositoryDAO
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.eq
import org.migor.feedless.generated.types.HttpFetchInput
import org.migor.feedless.generated.types.HttpGetRequestInput
import org.migor.feedless.generated.types.NullableLongUpdateOperationsInput
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.ScrapeActionInput
import org.migor.feedless.generated.types.ScrapeFlowInput
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.SourceUpdateInput
import org.migor.feedless.generated.types.SourcesUpdateInput
import org.migor.feedless.generated.types.StringLiteralOrVariableInput
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.session.RequestContext
import org.migor.feedless.session.SessionService
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserService
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class RepositoryServiceTest {

    private lateinit var repositoryDAO: RepositoryDAO

    private lateinit var sessionService: SessionService

    private lateinit var planConstraintsService: PlanConstraintsService

    private lateinit var repositoryService: RepositoryService
    private lateinit var sourceService: SourceService

    private lateinit var userId: UserId
    private lateinit var applicationContext: ApplicationContext

    @BeforeEach
    fun beforeEach() = runTest {
        repositoryDAO = mock(RepositoryDAO::class.java)
        sessionService = mock(SessionService::class.java)
        planConstraintsService = mock(PlanConstraintsService::class.java)
        applicationContext = mock(ApplicationContext::class.java)
        sourceService = mock(SourceService::class.java)

        repositoryService = RepositoryService(
//      mock(UserDAO::class.java),
            repositoryDAO,
            sessionService,
            mock(UserService::class.java),
            planConstraintsService,
            mock(DocumentService::class.java),
            mock(PropertyService::class.java),
            sourceService,
            applicationContext
        )
        `when`(applicationContext.getBean(eq(RepositoryService::class.java))).thenReturn(repositoryService)

        userId = randomUserId()
        val user = mock(User::class.java)
        `when`(user.id).thenReturn(userId)
        `when`(sessionService.user()).thenReturn(user)
        `when`(sessionService.activeProductFromRequest()).thenReturn(Vertical.rssProxy.fromDto())
        `when`(repositoryDAO.save(any2()))
            .thenAnswer { it.getArgument(0) }
    }

    @Test
    fun `given maxActiveCount is reached, when creating a new repositoru, then return error`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            runTest(context = RequestContext(userId = userId)) {
                `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(UserId::class.java)))
                    .thenReturn(true)

                repositoryService.create(
                    emptyList()
                )
            }
        }
    }

    @Test
    fun `create repos`() = runTest(context = RequestContext(userId = userId)) {
        `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(UserId::class.java)))
            .thenReturn(false)
        `when`(planConstraintsService.coerceVisibility(eq(null)))
            .thenReturn(EntityVisibility.isPrivate)

        repositoryService.create(
            listOf(
                RepositoryCreateInput(
                    product = Vertical.rssProxy,
                    sources = listOf(
                        SourceInput(
                            title = "wef",
                            flow = ScrapeFlowInput(
                                sequence = listOf(
                                    ScrapeActionInput(
                                        fetch = HttpFetchInput(
                                            get = HttpGetRequestInput(
                                                url = StringLiteralOrVariableInput(
                                                    literal = ""
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                        )
                    ),
                    title = "",
                    description = "",
                    refreshCron = "",
                    withShareKey = true
                )
            )
        )
    }

    @Test
    fun `given maxActiveCount is not reached, when creating a new repository, then repository is created`() =
        runTest(context = RequestContext(userId = userId)) {
            `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(UserId::class.java)))
                .thenReturn(false)
            `when`(planConstraintsService.coerceVisibility(Mockito.any()))
                .thenReturn(EntityVisibility.isPublic)

            val repositories = listOf(
                RepositoryCreateInput(
                    sources = emptyList(),
                    product = Vertical.rssProxy,
                    title = "",
                    description = "",
                    withShareKey = false
                )
            )
            val createdRepositories = repositoryService.create(
                repositories
            )

            assertThat(createdRepositories.size).isEqualTo(repositories.size)
        }

    @Test
    @Disabled("legacy feeds demand disabled authority checks")
    fun `requesting private repository is restricted to owner`() {
        assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
            runTest(context = RequestContext(userId = randomUserId())) {
                `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(UserId::class.java)))
                    .thenReturn(false)
                `when`(planConstraintsService.coerceVisibility(Mockito.any()))
                    .thenReturn(EntityVisibility.isPublic)

                repositoryService.findById(randomRepositoryId())
            }
        }
    }

    @Test
    fun `given user is owner, updating repository works`() = runTest(context = RequestContext(userId = userId)) {
        val repositoryId = randomRepositoryId()
        val data = RepositoryUpdateDataInput(
            nextUpdateAt = NullableLongUpdateOperationsInput(set = null)
        )
        val mockRepository = mock(RepositoryEntity::class.java)
        `when`(mockRepository.ownerId).thenReturn(userId.uuid)

        `when`(repositoryDAO.findById(any(UUID::class.java)))
            .thenReturn(Optional.of(mockRepository))

        // when
        val update = repositoryService.updateRepository(repositoryId, data)

        // then
        verify(mockRepository).triggerScheduledNextAt = any2()
        assertThat(update).isNotNull()
    }

    @Test
    fun `given user is not owner, updating repository fails`() {
        val repositoryId = randomRepositoryId()
        val mockRepository = mock(RepositoryEntity::class.java)
        `when`(mockRepository.ownerId).thenReturn(UUID.randomUUID())

        assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
            val mockInput = RepositoryUpdateDataInput()
            runTest(context = RequestContext(userId = userId)) {
                `when`(repositoryDAO.findById(any(UUID::class.java)))
                    .thenReturn(Optional.of(mockRepository))

                repositoryService.updateRepository(repositoryId, mockInput)
            }
        }
    }

    @Test
    fun `given updateRepository call, sources can be removed`() = runTest(context = RequestContext(userId = userId)) {
        val repositoryId = randomRepositoryId()
        val removeSources = listOf(randomSourceId())
        val data = RepositoryUpdateDataInput(
            sources = SourcesUpdateInput(
                remove = removeSources.map { it.uuid.toString() }
            )
        )
        val mockRepository = mock(RepositoryEntity::class.java)
        `when`(mockRepository.ownerId).thenReturn(userId.uuid)
        `when`(mockRepository.id).thenReturn(repositoryId.uuid)

        `when`(repositoryDAO.findById(any(UUID::class.java)))
            .thenReturn(Optional.of(mockRepository))

        // when
        repositoryService.updateRepository(repositoryId, data)

        // then
        verify(sourceService).deleteAllById(eq(repositoryId), eq(removeSources))
    }

    @Test
    fun `given updateRepository call, sources can be updated`() = runTest(context = RequestContext(userId = userId)) {
        val repositoryId = randomRepositoryId()
        val updateSources = listOf(mock(SourceUpdateInput::class.java))
        val data = RepositoryUpdateDataInput(
            sources = SourcesUpdateInput(
                update = updateSources,
            )
        )
        val repositoryEntity = mock(RepositoryEntity::class.java)
        `when`(repositoryEntity.ownerId).thenReturn(userId.uuid)
        `when`(repositoryEntity.id).thenReturn(repositoryId.uuid)

        `when`(repositoryDAO.findById(any(UUID::class.java)))
            .thenReturn(Optional.of(repositoryEntity))

        // when
        repositoryService.updateRepository(repositoryId, data)

        // then
        verify(sourceService).updateSources(eq(repositoryId), eq(updateSources))
    }

    @Test
    fun `given updateRepository call, sources can be added`() = runTest(context = RequestContext(userId = userId)) {
        val repositoryId = randomRepositoryId()
        val addSources = listOf(mock(SourceInput::class.java))
        val data = RepositoryUpdateDataInput(
            sources = SourcesUpdateInput(
                add = addSources,
            )
        )
        val repository = mock(RepositoryEntity::class.java)
        `when`(repository.ownerId).thenReturn(userId.uuid)
        `when`(repository.id).thenReturn(repositoryId.uuid)

        `when`(repositoryDAO.findById(any(UUID::class.java)))
            .thenReturn(Optional.of(repository))

        // when
        repositoryService.updateRepository(repositoryId, data)

        // then
        verify(sourceService).createSources(eq(userId), eq(addSources), eq(repositoryId))
    }

    @Test
    fun `given user is not owner, deleting repository fails`() {
        val repositoryId = randomRepositoryId()
        val mockRepository = mock(RepositoryEntity::class.java)
        `when`(mockRepository.ownerId).thenReturn(UUID.randomUUID())

        `when`(repositoryDAO.findById(any(UUID::class.java)))
            .thenReturn(Optional.of(mockRepository))

        assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
            runTest(context = RequestContext(userId = userId)) {
                repositoryService.delete(repositoryId)
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "0 */10 * * * *, 10, MINUTES",
            "0 */15 * * * *, 15, MINUTES",
            "0 */30 * * * *, 30, MINUTES",
            "0 0 * * * *, 1, HOURS",
            "0 0 */6 * * *, 6, HOURS",
            "0 0 */12 * * *, 12, HOURS",
            "0 0 0 * * *, 1, DAYS",
            "0 0 0 * * 0, 1, WEEKS"
        ]
    )
    fun calculateScheduledNextAt(cron: String, increment: Double, unit: String) {
        val chronoUnit = ChronoUnit.valueOf(unit)
        assertThat(chronoUnit.name).isEqualTo(unit)

        val now = LocalDateTime.now()

        val from = nextCronDate(cron, now)
        val to = nextCronDate(cron, from)
        val diff = Duration.between(from, to)
        val diffInUnit = when (chronoUnit) {
            ChronoUnit.MINUTES -> diff.toMinutes().toDouble()
            ChronoUnit.HOURS -> diff.toHours().toDouble()
            ChronoUnit.DAYS -> diff.toDays().toDouble()
            ChronoUnit.WEEKS -> diff.toDays() / 7.0
            else -> IllegalArgumentException()
        }
        assertThat(diffInUnit).isEqualTo(increment)
    }
}
