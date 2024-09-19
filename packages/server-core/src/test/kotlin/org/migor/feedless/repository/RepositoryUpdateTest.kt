package org.migor.feedless.repository

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.actions.ScrapeActionDAO
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.document.DocumentService
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
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.user.UserService
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*

class RepositoryUpdateTest {

  lateinit var repositoryService: RepositoryService
  lateinit var repositoryDAO: RepositoryDAO
  lateinit var sessionService: SessionService
  lateinit var planConstraintsService: PlanConstraintsService
  val corrId = "test"
  lateinit var repositoryId: UUID
  lateinit var ownerId: UUID
  lateinit var repository: RepositoryEntity
  lateinit var data: RepositoryUpdateDataInput

  @BeforeEach
  fun setUp() {
    repositoryDAO = mock(RepositoryDAO::class.java)
    sessionService = mock(SessionService::class.java)
    planConstraintsService = mock(PlanConstraintsService::class.java)

    repositoryService = spy(
      RepositoryService(
        mock(SourceDAO::class.java),
//      mock(UserDAO::class.java),
        repositoryDAO,
        sessionService,
        mock(UserService::class.java),
        planConstraintsService,
        mock(DocumentService::class.java),
        mock(PropertyService::class.java),
        mock(ScrapeActionDAO::class.java)
      )
    )

    repositoryId = UUID.randomUUID()
    ownerId = UUID.randomUUID()
    repository = mock(RepositoryEntity::class.java)
    `when`(repository.id).thenReturn(repositoryId)
    `when`(repository.ownerId).thenReturn(ownerId)
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
  fun `given repo does not exists, update will fail`() = runTest {
    assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
      runBlocking {
        repositoryService.update(corrId, repositoryId, data)
      }
    }
  }

  @Test
  fun `given current user has insuffieient priveleges, update will fail`() = runTest {
    `when`(repositoryDAO.findByIdWithSources(any(UUID::class.java))).thenReturn(repository)
    `when`(sessionService.userId()).thenReturn(UUID.randomUUID())
    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runBlocking {
        repositoryService.update(corrId, repositoryId, data)
      }
    }
  }

  @Test
  fun `given all requirements are met, update will work`() = runTest {
    `when`(sessionService.userId()).thenReturn(ownerId)
    `when`(planConstraintsService.auditCronExpression(any(String::class.java))).thenAnswer {
      it.arguments[0]
    }
    `when`(
      planConstraintsService.coerceMinScheduledNextAt(
        any(LocalDateTime::class.java),
        any(LocalDateTime::class.java),
        any(UUID::class.java),
        any(ProductCategory::class.java),
      )
    ).thenReturn(LocalDateTime.now())
    `when`(sessionService.activeProductFromRequest()).thenReturn(ProductCategory.rssProxy)
    `when`(repositoryDAO.findByIdWithSources(any(UUID::class.java))).thenReturn(repository)
    `when`(repositoryDAO.save(any(RepositoryEntity::class.java))).thenAnswer { it.arguments[0] }

    repositoryService.update(corrId, repositoryId, data)

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
    verify(repositoryDAO).save(any(RepositoryEntity::class.java))
  }

}
