package org.migor.feedless.document

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.generated.types.ProductCategory
import org.migor.feedless.generated.types.RepositoriesCreateInput
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.SinkOptionsInput
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserEntity
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.util.*


@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RepositoryServiceTest {

  private val corrId = "test"

  @Mock
  lateinit var repositoryDAO: RepositoryDAO

  @Mock
  lateinit var sessionService: SessionService

  @Mock
  lateinit var planConstraintsService: PlanConstraintsService

  @InjectMocks
  lateinit var repositoryService: RepositoryService

  private lateinit var userId: UUID

  @BeforeEach
  fun beforeEach() {
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
  fun `given maxActiveCount is reached, when creating a new repositoru, then return error`() {
    `when`(planConstraintsService.violatesScrapeSourceMaxActiveCount(any(UUID::class.java)))
      .thenReturn(true)

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      repositoryService.create(
        "-", RepositoriesCreateInput(
          repositories = emptyList()
        )
      )
    }
  }

  @Test
  fun `given maxActiveCount is not reached, when creating a new repository, then repository is created`() {
    `when`(planConstraintsService.violatesScrapeSourceMaxActiveCount(any(UUID::class.java)))
      .thenReturn(false)
    `when`(planConstraintsService.coerceVisibility(Mockito.anyString(), Mockito.any()))
      .thenReturn(EntityVisibility.isPublic)

    val repositories = listOf(
      RepositoryCreateInput(
        sources = emptyList(),
        product = ProductCategory.rssProxy,
        sinkOptions = SinkOptionsInput(
          title="",
          description="",
          withShareKey = false
        )
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
  fun `given user is owner, updating repository works`() {
    val ssId = UUID.randomUUID()
    val data = RepositoryUpdateDataInput()
    val mockRepository = mock(RepositoryEntity::class.java)
    `when`(mockRepository.ownerId).thenReturn(userId)

    `when`(repositoryDAO.findById(any(UUID::class.java)))
      .thenReturn(Optional.of(mockRepository))

    val update = repositoryService.update(corrId, ssId, data)
    assertThat(update).isNotNull()
  }

  @Test
  fun `given user is not owner, updating repository fails`() {
    val ssId = UUID.randomUUID()
    val mockRepository = mock(RepositoryEntity::class.java)
    `when`(mockRepository.ownerId).thenReturn(UUID.randomUUID())

    `when`(repositoryDAO.findById(any(UUID::class.java)))
      .thenReturn(Optional.of(mockRepository))

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      val mockInput = RepositoryUpdateDataInput()
      repositoryService.update(corrId, ssId, mockInput)
    }
  }

  @Test
  fun `given user is not owner, deleting repository fails`() {
    val ssId = UUID.randomUUID()
    val mockRepository = mock(RepositoryEntity::class.java)
    `when`(mockRepository.ownerId).thenReturn(UUID.randomUUID())

    `when`(repositoryDAO.findById(any(UUID::class.java)))
      .thenReturn(Optional.of(mockRepository))

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      repositoryService.delete(corrId, ssId)
    }
  }
}

fun <T> anyList(): List<T> = Mockito.anyList<T>()
fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
fun <T> anyOrNull(type: Class<T>): T? = Mockito.any<T?>(type)
fun <T> eq(type: T): T = Mockito.eq<T>(type) ?: type
