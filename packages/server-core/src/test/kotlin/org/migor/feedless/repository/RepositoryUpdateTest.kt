package org.migor.feedless.repository

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.Vertical
import org.migor.feedless.any2
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.capability.UserCapability
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
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.session.SessionService
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserService
import org.migor.feedless.util.JsonSerializer.toJson
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationContext
import java.time.LocalDateTime


class RepositoryUpdateTest {

  private lateinit var repositoryService: RepositoryService
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var sessionService: SessionService
  private lateinit var planConstraintsService: PlanConstraintsService
  private lateinit var repositoryId: RepositoryId
  private lateinit var ownerId: UserId
  private lateinit var repository: Repository
  private lateinit var data: RepositoryUpdateDataInput
  private lateinit var applicationContext: ApplicationContext
  private lateinit var sourceService: SourceService
  private lateinit var capabilityService: CapabilityService
  private val currentUserId = randomUserId()

  @BeforeEach
  fun setUp() {
    repositoryRepository = mock(RepositoryRepository::class.java)
    sessionService = mock(SessionService::class.java)
    planConstraintsService = mock(PlanConstraintsService::class.java)
    applicationContext = mock(ApplicationContext::class.java)
    sourceService = mock(SourceService::class.java)
    capabilityService = mock(CapabilityService::class.java)
    `when`(capabilityService.getCapability(UserCapability.ID))
      .thenReturn(UnresolvedCapability(UserCapability.ID, toJson(currentUserId)))

    repositoryService = spy(
      RepositoryService(
//      mock(UserDAO::class.java),
        repositoryRepository,
        sessionService,
        mock(UserService::class.java),
        planConstraintsService,
        mock(DocumentService::class.java),
        mock(PropertyService::class.java),
        sourceService,
        applicationContext,
        capabilityService
      )
    )

    `when`(applicationContext.getBean(eq(RepositoryService::class.java))).thenReturn(repositoryService)

    repositoryId = randomRepositoryId()
    ownerId = randomUserId()
    repository = Repository(
      id = repositoryId,
      ownerId = this@RepositoryUpdateTest.ownerId,
      lastUpdatedAt = LocalDateTime.now(),
      title = "old-title",
      description = "old-description",
      sourcesSyncCron = "0 0 * * * *",
      product = Vertical.rssProxy
    )
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
      runTest {
        mockCurrentUser(currentUserId)
        repositoryService.updateRepository(repositoryId, data)
      }
    }
  }

  @Test
  fun `given current user has insuffieient priveleges, update will fail`() {
    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest {
        mockCurrentUser(randomUserId())
        `when`(repositoryRepository.findById(any2())).thenReturn(repository)
        repositoryService.updateRepository(repositoryId, data)
      }
    }
  }

  @Test
  fun `given all requirements are met, repository data will be updated`() = runTest {
    mockCurrentUser(ownerId)

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
    `when`(planConstraintsService.coerceVisibility(any2())).thenAnswer {
      it.arguments[0] ?: EntityVisibility.isPrivate
    }
    mockActiveProductFromRequest()
    `when`(repositoryRepository.findById(any2())).thenReturn(repository)

    var savedRepo: Repository? = null
    `when`(repositoryRepository.save(any2())).thenAnswer {
      savedRepo = it.arguments[0] as Repository
      savedRepo
    }

    repositoryService.updateRepository(repositoryId, data)

    // Verify the repository was saved with updated values
    assertThat(savedRepo).isNotNull
    assertThat(savedRepo!!.title).isEqualTo("new-title")
    assertThat(savedRepo.description).isEqualTo("new-description")
    assertThat(savedRepo.sourcesSyncCron).isEqualTo("* * * * * *")
  }

  private fun mockCurrentUser(currentUserId: UserId) {
    `when`(capabilityService.getCapability(UserCapability.ID))
      .thenReturn(UnresolvedCapability(UserCapability.ID, toJson(currentUserId)))
  }

  @Test
  fun `given a valid update request, sources can be added`() = runTest {
    // given
    mockCurrentUser(ownerId)
    `when`(repositoryRepository.findById(any2())).thenReturn(repository)
    `when`(planConstraintsService.auditCronExpression(any2())).thenAnswer { it.arguments[0] }
    `when`(planConstraintsService.coerceVisibility(any2())).thenAnswer {
      it.arguments[0] ?: EntityVisibility.isPrivate
    }
    `when`(
      planConstraintsService.coerceMinScheduledNextAt(
        any2(),
        any2(),
        any2(),
        any2()
      )
    ).thenReturn(LocalDateTime.now())
    mockActiveProductFromRequest()
    mockRepositorySave()

    // when
    repositoryService.updateRepository(repositoryId, data)

    // then
    verify(sourceService).createSources(any2(), any2(), any2())
  }

  @Test
  fun `given a valid update request, sources can be updated`() = runTest {
    // given
    mockCurrentUser(ownerId)
    `when`(repositoryRepository.findById(any2())).thenReturn(repository)
    `when`(planConstraintsService.auditCronExpression(any2())).thenAnswer { it.arguments[0] }
    `when`(planConstraintsService.coerceVisibility(any2())).thenAnswer {
      it.arguments[0] ?: EntityVisibility.isPrivate
    }
    `when`(
      planConstraintsService.coerceMinScheduledNextAt(
        any2(),
        any2(),
        any2(),
        any2()
      )
    ).thenReturn(LocalDateTime.now())
    mockActiveProductFromRequest()
    mockRepositorySave()

    // when
    repositoryService.updateRepository(repositoryId, data)

    // then
    verify(sourceService).updateSources(any2(), any2())
  }

  @Test
  fun `given a valid update request, sources can be removed`() = runTest {
    // given
    mockCurrentUser(ownerId)
    `when`(repositoryRepository.findById(any2())).thenReturn(repository)
    `when`(planConstraintsService.auditCronExpression(any2())).thenAnswer { it.arguments[0] }
    `when`(planConstraintsService.coerceVisibility(any2())).thenAnswer {
      it.arguments[0] ?: EntityVisibility.isPrivate
    }
    `when`(
      planConstraintsService.coerceMinScheduledNextAt(
        any2(),
        any2(),
        any2(),
        any2()
      )
    ).thenReturn(LocalDateTime.now())
    mockActiveProductFromRequest()
    mockRepositorySave()

    // when
    repositoryService.updateRepository(repositoryId, data)

    // then
    verify(sourceService).deleteAllById(any2(), any2())
  }

  private suspend fun mockRepositorySave() {
    `when`(repositoryRepository.save(any2())).thenAnswer { it.arguments[0] }
  }

  private suspend fun mockActiveProductFromRequest() {
    `when`(sessionService.activeProductFromRequest()).thenReturn(Vertical.rssProxy)
  }

}
