package org.migor.feedless.repository

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.Vertical
import org.migor.feedless.any2
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.AppConfig
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.group.GroupId
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.source.SourceUseCase
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime


class RepositoryUpdateTest {

  private lateinit var repositoryUseCase: RepositoryUseCase
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var planConstraintsService: PlanConstraintsService
  private lateinit var repositoryId: RepositoryId
  private lateinit var ownerId: UserId
  private lateinit var repository: Repository
  private lateinit var data: RepositoryUpdate
  private lateinit var sourceUseCase: SourceUseCase
  private lateinit var repositoryGuard: RepositoryGuard
  private lateinit var documentUseCase: DocumentUseCase
  private val currentUserId = randomUserId()

  @BeforeEach
  fun setUp() = runTest {
    repositoryRepository = mock(RepositoryRepository::class.java)
    planConstraintsService = mock(PlanConstraintsService::class.java)
    sourceUseCase = mock(SourceUseCase::class.java)
    documentUseCase = mock(DocumentUseCase::class.java)

    repositoryGuard = mock(RepositoryGuard::class.java)
    repositoryUseCase = spy(
      RepositoryUseCase(
        repositoryRepository,
        planConstraintsService,
        documentUseCase,
        mock(AppConfig::class.java),
        sourceUseCase,
        repositoryGuard,
      )
    )

    repositoryId = randomRepositoryId()
    ownerId = randomUserId()
    repository = Repository(
      id = repositoryId,
      ownerId = this@RepositoryUpdateTest.ownerId,
      groupId = GroupId(),
      lastUpdatedAt = LocalDateTime.now(),
      title = "old-title",
      description = "old-description",
      sourcesSyncCron = "0 0 * * * *",
      product = Vertical.rssProxy
    )

    `when`(repositoryGuard.requireWrite(repositoryId)).thenReturn(repository)

    data = RepositoryUpdate(
      description = "new-description",
      refreshCron = "* * * * * *",
      title = "new-title",
      pushNotificationsEnabled = true,
      visibility = EntityVisibility.isPrivate,
      retentionMaxCapacity = null,
      retentionMaxAgeDays = null,
      retentionMaxAgeDaysReferenceField = MaxAgeDaysDateField.createdAt,
      plugins = listOf(),
      nextUpdateAt = LocalDateTime.ofEpochSecond(1, 0, java.time.ZoneOffset.UTC),
      sources = RepositorySourcesUpdate(
        remove = emptyList(),
        update = emptyList(),
        add = emptyList()
      ),
    )
  }

  @Test
  fun `given current user has insuffieient priveleges, update will fail`() {
    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
        `when`(repositoryRepository.findById(any2())).thenReturn(repository)
        repositoryUseCase.updateRepository(repositoryId, data)
      }
    }
  }

  @Test
  fun `given all requirements are met, repository data will be updated`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {

      `when`(planConstraintsService.auditCronExpression(any2())).thenAnswer {
        it.arguments[0]
      }
      `when`(
        planConstraintsService.coerceMinScheduledNextAt(
          any2(),
          any2(),
          any2(),
        )
      ).thenReturn(LocalDateTime.now())
      `when`(planConstraintsService.coerceVisibility(any2(), any2())).thenAnswer {
        it.arguments[1] as? EntityVisibility ?: EntityVisibility.isPrivate
      }
      `when`(repositoryRepository.findById(any2())).thenReturn(repository)

      var savedRepo: Repository? = null
      `when`(repositoryRepository.save(any2())).thenAnswer {
        savedRepo = it.arguments[0] as Repository
        savedRepo
      }

      repositoryUseCase.updateRepository(repositoryId, data)

      assertThat(savedRepo).isNotNull
      val saved = savedRepo!!
      assertThat(saved.title).isEqualTo("new-title")
      assertThat(saved.description).isEqualTo("new-description")
      assertThat(saved.sourcesSyncCron).isEqualTo("* * * * * *")
    }

  @Test
  fun `given a valid update request, sources can be added`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {
      `when`(repositoryRepository.findById(any2())).thenReturn(repository)
      `when`(planConstraintsService.auditCronExpression(any2())).thenAnswer { it.arguments[0] }
      `when`(planConstraintsService.coerceVisibility(any2(), any2())).thenAnswer {
        it.arguments[1] as? EntityVisibility ?: EntityVisibility.isPrivate
      }
      `when`(
        planConstraintsService.coerceMinScheduledNextAt(
          any2(),
          any2(),
          any2(),
        )
      ).thenReturn(LocalDateTime.now())
      mockRepositorySave()

      repositoryUseCase.updateRepository(repositoryId, data)

      verify(sourceUseCase).createSources(any2(), any2())
    }

  @Test
  fun `given a valid update request, sources can be updated`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {
      `when`(repositoryRepository.findById(any2())).thenReturn(repository)
      `when`(planConstraintsService.auditCronExpression(any2())).thenAnswer { it.arguments[0] }
      `when`(planConstraintsService.coerceVisibility(any2(), any2())).thenAnswer {
        it.arguments[1] as? EntityVisibility ?: EntityVisibility.isPrivate
      }
      `when`(
        planConstraintsService.coerceMinScheduledNextAt(
          any2(),
          any2(),
          any2()
        )
      ).thenReturn(LocalDateTime.now())
      mockRepositorySave()

      repositoryUseCase.updateRepository(repositoryId, data)

      verify(sourceUseCase).updateSources(any2(), any2())
    }

  @Test
  fun `given a valid update request, sources can be removed`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {
      `when`(repositoryRepository.findById(any2())).thenReturn(repository)
      `when`(planConstraintsService.auditCronExpression(any2())).thenAnswer { it.arguments[0] }
      `when`(planConstraintsService.coerceVisibility(any2(), any2())).thenAnswer {
        it.arguments[1] as? EntityVisibility ?: EntityVisibility.isPrivate
      }
      `when`(
        planConstraintsService.coerceMinScheduledNextAt(
          any2(),
          any2(),
          any2()
        )
      ).thenReturn(LocalDateTime.now())
      mockRepositorySave()

      repositoryUseCase.updateRepository(repositoryId, data)

      verify(sourceUseCase).deleteAllById(any2(), any2())
    }

  @Test
  fun `clear retention maxAgeDays applies retention strategy and clears field`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {
      `when`(repositoryRepository.findById(any2())).thenReturn(repository)
      var savedRepo: Repository? = null
      `when`(repositoryRepository.save(any2())).thenAnswer {
        savedRepo = it.arguments[0] as Repository
        savedRepo
      }

      repositoryUseCase.updateRepository(repositoryId, RepositoryUpdate(clearRetentionMaxAgeDays = true))

      verify(documentUseCase).applyRetentionStrategy(repositoryId)
      assertThat(savedRepo?.retentionMaxAgeDays).isNull()
    }

  @Test
  fun `schedule next update now coerces current time`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {
      `when`(repositoryRepository.findById(any2())).thenReturn(repository)
      val coercedNextAt = LocalDateTime.of(2026, 7, 18, 12, 0)
      `when`(
        planConstraintsService.coerceMinScheduledNextAt(
          any2(),
          any2(),
          any2(),
        )
      ).thenReturn(coercedNextAt)
      var savedRepo: Repository? = null
      `when`(repositoryRepository.save(any2())).thenAnswer {
        savedRepo = it.arguments[0] as Repository
        savedRepo
      }

      repositoryUseCase.updateRepository(repositoryId, RepositoryUpdate(scheduleNextUpdateNow = true))

      verify(planConstraintsService).coerceMinScheduledNextAt(
        any2(),
        any2(),
        any2(),
      )
      assertThat(savedRepo?.triggerScheduledNextAt).isEqualTo(coercedNextAt)
    }

  @Test
  fun `retention age reference field is persisted`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {
      `when`(repositoryRepository.findById(any2())).thenReturn(repository)
      var savedRepo: Repository? = null
      `when`(repositoryRepository.save(any2())).thenAnswer {
        savedRepo = it.arguments[0] as Repository
        savedRepo
      }

      repositoryUseCase.updateRepository(
        repositoryId,
        RepositoryUpdate(retentionMaxAgeDaysReferenceField = MaxAgeDaysDateField.publishedAt),
      )

      assertThat(savedRepo?.retentionMaxAgeDaysReferenceField).isEqualTo(MaxAgeDaysDateField.publishedAt)
    }

  private fun mockRepositorySave() {
    `when`(repositoryRepository.save(any2())).thenAnswer { it.arguments[0] }
  }

}
