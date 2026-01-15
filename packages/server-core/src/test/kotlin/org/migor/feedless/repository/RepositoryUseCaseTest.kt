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
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.eq
import org.migor.feedless.generated.types.HttpFetchInput
import org.migor.feedless.generated.types.HttpGetRequestInput
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.ScrapeActionInput
import org.migor.feedless.generated.types.ScrapeFlowInput
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.SourceUpdateInput
import org.migor.feedless.generated.types.SourcesUpdateInput
import org.migor.feedless.generated.types.StringLiteralOrVariableInput
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.group.GroupId
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.source.SourceUseCase
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class RepositoryUseCaseTest {

  private lateinit var repositoryRepository: RepositoryRepository

  private lateinit var planConstraintsService: PlanConstraintsService

  private lateinit var repositoryUseCase: RepositoryUseCase
  private lateinit var sourceUseCase: SourceUseCase
  private lateinit var repositoryGuard: RepositoryGuard

  private lateinit var userId: UserId

  @BeforeEach
  fun beforeEach() = runTest {
    userId = randomUserId()
    repositoryRepository = mock(RepositoryRepository::class.java)
    planConstraintsService = mock(PlanConstraintsService::class.java)
    sourceUseCase = mock(SourceUseCase::class.java)
    repositoryGuard = mock(RepositoryGuard::class.java)

    repositoryUseCase = RepositoryUseCase(
      repositoryRepository,
      planConstraintsService,
      mock(DocumentUseCase::class.java),
      mock(PropertyService::class.java),
      sourceUseCase,
      repositoryGuard
    )

    val user = mock(User::class.java)
    `when`(user.id).thenReturn(userId)
    `when`(repositoryRepository.save(any2()))
      .thenAnswer { it.getArgument(0) }
    `when`(repositoryRepository.countByGroupId(any(GroupId::class.java)))
      .thenReturn(0)
  }

  @Test
  fun `given maxActiveCount is reached, when creating a new repositoru, then return error`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
        `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(GroupId::class.java)))
          .thenReturn(true)

        repositoryUseCase.create(
          emptyList()
        )
      }
    }
  }

  @Test
  fun `create repos`() = runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
    `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(GroupId::class.java)))
      .thenReturn(false)
    `when`(planConstraintsService.coerceVisibility(any(GroupId::class.java), eq(null)))
      .thenReturn(EntityVisibility.isPrivate)

    repositoryUseCase.create(
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
    runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
      `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(GroupId::class.java)))
        .thenReturn(false)
      `when`(planConstraintsService.coerceVisibility(any2(), Mockito.any()))
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
      val createdRepositories = repositoryUseCase.create(
        repositories
      )

      assertThat(createdRepositories.size).isEqualTo(repositories.size)
    }

  @Test
  @Disabled("legacy feeds demand disabled authority checks")
  fun `requesting private repository is restricted to owner`() {
    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
        `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(GroupId::class.java)))
          .thenReturn(false)
        `when`(planConstraintsService.coerceVisibility(any2(), Mockito.any()))
          .thenReturn(EntityVisibility.isPublic)

        repositoryUseCase.findById(randomRepositoryId())
      }
    }
  }

//  @Test
//  fun `given user is owner, updating repository works`() = runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
//    val repositoryId = randomRepositoryId()
//    val data = RepositoryUpdateDataInput(
//      nextUpdateAt = NullableLongUpdateOperationsInput(set = null)
//    )
//    val mockRepository = mock(Repository::class.java)
//    `when`(mockRepository.ownerId).thenReturn(userId)
//
//    `when`(repositoryDAO.findById(any(RepositoryId::class.java)))
//      .thenReturn(mockRepository)
//
//    // when
//    val update = repositoryService.updateRepository(repositoryId, data)
//
//    // then
//    verify(mockRepository).triggerScheduledNextAt = any2()
//    assertThat(update).isNotNull()
//  }

  @Test
  fun `given user is not owner, updating repository fails`() {
    val repositoryId = randomRepositoryId()
    val otherOwnerId = UserId()
    val mockRepository = Repository(
      id = repositoryId,
      title = "test",
      ownerId = otherOwnerId,
      groupId = GroupId()
    )

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      val mockInput = RepositoryUpdateDataInput()
      runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
        `when`(repositoryGuard.requireWrite(any(RepositoryId::class.java)))
          .thenReturn(mockRepository)

        repositoryUseCase.updateRepository(repositoryId, mockInput)
      }
    }
  }

  @Test
  fun `given updateRepository call, sources can be removed`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
      val repositoryId = randomRepositoryId()
      val removeSources = listOf(randomSourceId())
      val data = RepositoryUpdateDataInput(
        sources = SourcesUpdateInput(
          remove = removeSources.map { it.uuid.toString() }
        )
      )
      val mockRepository = Repository(
        id = repositoryId,
        title = "test",
        ownerId = userId,
        groupId = GroupId(),
      )

      `when`(repositoryGuard.requireWrite(any(RepositoryId::class.java)))
        .thenReturn(mockRepository)
      `when`(repositoryRepository.save(any2())).thenAnswer { it.arguments[0] }

      // when
      repositoryUseCase.updateRepository(repositoryId, data)

      // then
      verify(sourceUseCase).deleteAllById(eq(repositoryId), eq(removeSources))
    }

  @Test
  fun `given updateRepository call, sources can be updated`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
      val repositoryId = randomRepositoryId()
      val updateSources = listOf(mock(SourceUpdateInput::class.java))
      val data = RepositoryUpdateDataInput(
        sources = SourcesUpdateInput(
          update = updateSources,
        )
      )
      val repository = Repository(
        id = repositoryId,
        title = "test",
        ownerId = userId,
        groupId = GroupId(),
      )

      `when`(repositoryGuard.requireWrite(any(RepositoryId::class.java)))
        .thenReturn(repository)
      `when`(repositoryRepository.save(any2())).thenAnswer { it.arguments[0] }

      // when
      repositoryUseCase.updateRepository(repositoryId, data)

      // then
      verify(sourceUseCase).updateSources(eq(repositoryId), eq(updateSources))
    }

  @Test
  fun `given updateRepository call, sources can be added`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
      val repositoryId = randomRepositoryId()
      val addSources = listOf(mock(SourceInput::class.java))
      val data = RepositoryUpdateDataInput(
        sources = SourcesUpdateInput(
          add = addSources,
        )
      )
      val repository = Repository(
        id = repositoryId,
        title = "test",
        ownerId = userId,
        groupId = GroupId(),
      )

      `when`(repositoryGuard.requireWrite(any(RepositoryId::class.java)))
        .thenReturn(repository)
      `when`(repositoryRepository.save(any2())).thenAnswer { it.arguments[0] }

      // when
      repositoryUseCase.updateRepository(repositoryId, data)

      // then
      verify(sourceUseCase).createSources(eq(addSources), eq(repositoryId))
    }

  @Test
  fun `given user is not owner, deleting repository fails`() = runTest {
    val repositoryId = randomRepositoryId()
    val mockRepository = Repository(
      id = repositoryId,
      title = "test",
      ownerId = UserId(),
      groupId = GroupId(),
    )

    `when`(repositoryRepository.findById(any(RepositoryId::class.java)))
      .thenReturn(mockRepository)

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
        repositoryUseCase.delete(repositoryId)
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
