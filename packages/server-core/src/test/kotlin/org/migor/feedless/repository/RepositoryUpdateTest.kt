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
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentUseCase
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
import org.migor.feedless.group.GroupId
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
  private lateinit var data: RepositoryUpdateDataInput
  private lateinit var sourceUseCase: SourceUseCase
  private lateinit var repositoryGuard: RepositoryGuard
  private val currentUserId = randomUserId()

  @BeforeEach
  fun setUp() = runTest {
    repositoryRepository = mock(RepositoryRepository::class.java)
    planConstraintsService = mock(PlanConstraintsService::class.java)
    sourceUseCase = mock(SourceUseCase::class.java)

    repositoryGuard = mock(RepositoryGuard::class.java)
    repositoryUseCase = spy(
      RepositoryUseCase(
        repositoryRepository,
        planConstraintsService,
        mock(DocumentUseCase::class.java),
        mock(PropertyService::class.java),
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

      // Verify the repository was saved with updated values
      assertThat(savedRepo).isNotNull
      val saved = savedRepo!!
      assertThat(saved.title).isEqualTo("new-title")
      assertThat(saved.description).isEqualTo("new-description")
      assertThat(saved.sourcesSyncCron).isEqualTo("* * * * * *")
    }

  @Test
  fun `given a valid update request, sources can be added`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {
      // given
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

      // when
      repositoryUseCase.updateRepository(repositoryId, data)

      // then
      verify(sourceUseCase).createSources(any2(), any2())
    }

  @Test
  fun `given a valid update request, sources can be updated`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {
      // given
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

      // when
      repositoryUseCase.updateRepository(repositoryId, data)

      // then
      verify(sourceUseCase).updateSources(any2(), any2())
    }

  @Test
  fun `given a valid update request, sources can be removed`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {
      // given
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

      // when
      repositoryUseCase.updateRepository(repositoryId, data)

      // then
      verify(sourceUseCase).deleteAllById(any2(), any2())
    }

  private fun mockRepositorySave() {
    `when`(repositoryRepository.save(any2())).thenAnswer { it.arguments[0] }
  }

}
