package org.migor.feedless.repository

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.actions.ScrapeActionDAO
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.document.DocumentService
import org.migor.feedless.generated.types.HttpFetchInput
import org.migor.feedless.generated.types.HttpGetRequestInput
import org.migor.feedless.generated.types.ProductCategory
import org.migor.feedless.generated.types.RepositoriesCreateInput
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.ScrapeActionInput
import org.migor.feedless.generated.types.ScrapeFlowInput
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.StringLiteralOrVariableInput
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.session.SessionService
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.mockito.ArgumentMatcher
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RepositoryServiceTest {

  private lateinit var sourceDAO: SourceDAO
  private val corrId = "test"

  private lateinit var repositoryDAO: RepositoryDAO

  private lateinit var sessionService: SessionService

  private lateinit var planConstraintsService: PlanConstraintsService

  private lateinit var repositoryService: RepositoryService

  private lateinit var userId: UUID

  @BeforeEach
  fun beforeEach() = runTest {
    repositoryDAO = mock(RepositoryDAO::class.java)
    sessionService = mock(SessionService::class.java)
    planConstraintsService = mock(PlanConstraintsService::class.java)
    sourceDAO = mock(SourceDAO::class.java)
    repositoryService = RepositoryService(
      sourceDAO,
//      mock(UserDAO::class.java),
      repositoryDAO,
      sessionService,
      mock(UserService::class.java),
      planConstraintsService,
      mock(DocumentService::class.java),
      mock(PropertyService::class.java),
      mock(ScrapeActionDAO::class.java)
    )

    userId = UUID.randomUUID()
    val user = mock(UserEntity::class.java)
    `when`(user.id).thenReturn(userId)
    `when`(sessionService.userId()).thenReturn(userId)
    `when`(sessionService.user(any(String::class.java))).thenReturn(user)
    `when`(sessionService.activeProductFromRequest()).thenReturn(ProductCategory.rssProxy.fromDto())
    `when`(repositoryDAO.save(any(RepositoryEntity::class.java)))
      .thenAnswer { it.getArgument(0) }
  }

  @Test
  fun `given maxActiveCount is reached, when creating a new repositoru, then return error`() = runTest {
    `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(UUID::class.java)))
      .thenReturn(true)

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runBlocking {
        repositoryService.create(
          "-", RepositoriesCreateInput(
            repositories = emptyList()
          )
        )
      }
    }
  }

  @Test
  fun `create repos`() = runTest {
    `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(UUID::class.java)))
      .thenReturn(false)
    `when`(planConstraintsService.coerceVisibility(any(String::class.java), eq(null)))
      .thenReturn(EntityVisibility.isPrivate)
    `when`(sourceDAO.save(any(SourceEntity::class.java)))
      .thenAnswer { it.arguments[0] }

    repositoryService.create(
      "-", RepositoriesCreateInput(
        repositories = listOf(
          RepositoryCreateInput(
            product = ProductCategory.rssProxy,
            sources = listOf(
              SourceInput(
                title = "",
                flow = ScrapeFlowInput(
                  sequence = listOf(
                    ScrapeActionInput(
                      fetch = HttpFetchInput(get = HttpGetRequestInput(url = StringLiteralOrVariableInput(literal = "")))
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
    )
  }

  @Test
  fun `given maxActiveCount is not reached, when creating a new repository, then repository is created`() = runTest {
    `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(UUID::class.java)))
      .thenReturn(false)
    `when`(planConstraintsService.coerceVisibility(Mockito.anyString(), Mockito.any()))
      .thenReturn(EntityVisibility.isPublic)

    val repositories = listOf(
      RepositoryCreateInput(
        sources = emptyList(),
        product = ProductCategory.rssProxy,
        title = "",
        description = "",
        withShareKey = false
      )
    )
    val createdRepositories = repositoryService.create(
      corrId, RepositoriesCreateInput(
        repositories = repositories
      )
    )

    assertThat(createdRepositories.size).isEqualTo(repositories.size)
  }

  @Test
  @Disabled("legacy feeds demand disabled authority checks")
  fun `requesting private repository is restricted to owner`() = runTest {
    `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(UUID::class.java)))
      .thenReturn(false)
    `when`(planConstraintsService.coerceVisibility(Mockito.anyString(), Mockito.any()))
      .thenReturn(EntityVisibility.isPublic)
    `when`(sessionService.userId())
      .thenReturn(UUID.randomUUID())

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runBlocking {
        repositoryService.findById(corrId, UUID.randomUUID())
      }
    }
  }

  @Test
  fun `given user is owner, updating repository works`() = runTest {
    val ssId = UUID.randomUUID()
    val data = RepositoryUpdateDataInput()
    val mockRepository = mock(RepositoryEntity::class.java)
    `when`(mockRepository.ownerId).thenReturn(userId)

    `when`(repositoryDAO.findByIdWithSources(any(UUID::class.java)))
      .thenReturn(mockRepository)

    val update = repositoryService.update(corrId, ssId, data)
    assertThat(update).isNotNull()
  }

  @Test
  fun `given user is not owner, updating repository fails`() = runTest {
    val ssId = UUID.randomUUID()
    val mockRepository = mock(RepositoryEntity::class.java)
    `when`(mockRepository.ownerId).thenReturn(UUID.randomUUID())

    `when`(repositoryDAO.findByIdWithSources(any(UUID::class.java)))
      .thenReturn(mockRepository)

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      val mockInput = RepositoryUpdateDataInput()
      runBlocking {
        repositoryService.update(corrId, ssId, mockInput)
      }
    }
  }

  @Test
  fun `given user is not owner, deleting repository fails`() = runTest {
    val ssId = UUID.randomUUID()
    val mockRepository = mock(RepositoryEntity::class.java)
    `when`(mockRepository.ownerId).thenReturn(UUID.randomUUID())

    `when`(repositoryDAO.findById(any(UUID::class.java)))
      .thenReturn(Optional.of(mockRepository))

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runBlocking {
        repositoryService.delete(corrId, ssId)
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


fun <T> anyList(): List<T> = Mockito.anyList<T>()
fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
fun <T> argThat(matcher: ArgumentMatcher<T>): T = Mockito.argThat<T>(matcher)
fun <T> anyOrNull(type: Class<T>): T? = Mockito.any<T?>(type)
fun <T> eq(type: T): T = Mockito.eq<T>(type) ?: type
