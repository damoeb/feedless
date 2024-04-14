package org.migor.feedless.source

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.repositories.SourceSubscriptionDAO
import org.migor.feedless.generated.types.ProductName
import org.migor.feedless.generated.types.SinkOptionsInput
import org.migor.feedless.generated.types.SourceSubscriptionCreateInput
import org.migor.feedless.generated.types.SourceSubscriptionsCreateInput
import org.migor.feedless.generated.types.UpdateSinkOptionsDataInput
import org.migor.feedless.plan.PlanConstraintsService
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
class SourceSubscriptionServiceTest {

  private val corrId = "test"

  @Mock
  lateinit var sourceSubscriptionDAO: SourceSubscriptionDAO

  @Mock
  lateinit var sessionService: SessionService

  @Mock
  lateinit var planConstraintsService: PlanConstraintsService

  @InjectMocks
  lateinit var sourceSubscriptionService: SourceSubscriptionService

  private lateinit var userId: UUID

  @BeforeEach
  fun beforeEach() {
    userId = UUID.randomUUID()
    val user = mock(UserEntity::class.java)
    `when`(user.id).thenReturn(userId)
    `when`(sessionService.userId()).thenReturn(userId)
    `when`(sessionService.user(any(String::class.java))).thenReturn(user)
    `when`(sourceSubscriptionDAO.save(any(SourceSubscriptionEntity::class.java)))
      .thenAnswer { it.getArgument(0) }
  }

  @Test
  fun `given maxActiveCount is reached, when creating a new sourceSubscription, then return error`() {
    `when`(planConstraintsService.violatesScrapeSourceMaxActiveCount(any(UUID::class.java)))
      .thenReturn(true)

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      sourceSubscriptionService.create(
        "-", SourceSubscriptionsCreateInput.newBuilder()
          .subscriptions(listOf())
          .build()
      )
    }
  }

  @Test
  fun `given maxActiveCount is not reached, when creating a new sourceSubscription, then sourceSubscription is created`() {
    `when`(planConstraintsService.violatesScrapeSourceMaxActiveCount(any(UUID::class.java)))
      .thenReturn(false)
    `when`(planConstraintsService.coerceVisibility(Mockito.any()))
      .thenReturn(EntityVisibility.isPublic)

    val sourceSubscriptions = listOf<SourceSubscriptionCreateInput>(
      SourceSubscriptionCreateInput.newBuilder()
        .sources(listOf())
//        .sourceOptions(
//          SourceOptionsInput.newBuilder()
//            .refreshCron("")
//            .build()
//        )
        .product(ProductName.feedless)
        .sinkOptions(
          SinkOptionsInput.newBuilder()
            .title("")
            .description("")
            .build()
        )
        .build()
    )
    val createdSourceSubscriptions = sourceSubscriptionService.create(
      corrId, SourceSubscriptionsCreateInput.newBuilder()
        .subscriptions(sourceSubscriptions)
        .build()
    )

    assertThat(createdSourceSubscriptions.size).isEqualTo(sourceSubscriptions.size)
  }

  @Test
  fun `given user is owner, updating SourceSubscription works`() {
    val ssId = UUID.randomUUID()
    val data = UpdateSinkOptionsDataInput.newBuilder()
      .build()
    val mockSourceSubscription = mock(SourceSubscriptionEntity::class.java)
    `when`(mockSourceSubscription.ownerId).thenReturn(userId)

    `when`(sourceSubscriptionDAO.findById(any(UUID::class.java)))
      .thenReturn(Optional.of(mockSourceSubscription))

    val update = sourceSubscriptionService.update(corrId, ssId, data)
    assertThat(update).isNotNull()
  }

  @Test
  fun `given user is not owner, updating SourceSubscription fails`() {
    val ssId = UUID.randomUUID()
    val mockSourceSubscription = mock(SourceSubscriptionEntity::class.java)
    `when`(mockSourceSubscription.ownerId).thenReturn(UUID.randomUUID())

    `when`(sourceSubscriptionDAO.findById(any(UUID::class.java)))
      .thenReturn(Optional.of(mockSourceSubscription))

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      sourceSubscriptionService.update(corrId, ssId, mock(UpdateSinkOptionsDataInput::class.java))
    }
  }

  @Test
  fun `given user is not owner, deleting SourceSubscription fails`() {
    val ssId = UUID.randomUUID()
    val mockSourceSubscription = mock(SourceSubscriptionEntity::class.java)
    `when`(mockSourceSubscription.ownerId).thenReturn(UUID.randomUUID())

    `when`(sourceSubscriptionDAO.findById(any(UUID::class.java)))
      .thenReturn(Optional.of(mockSourceSubscription))

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      sourceSubscriptionService.delete(corrId, ssId)
    }
  }
}

fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
fun <T> eq(type: T): T = Mockito.eq<T>(type)
