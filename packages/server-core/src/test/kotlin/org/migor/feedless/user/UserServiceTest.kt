package org.migor.feedless.user

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.document.any
import org.migor.feedless.document.eq
import org.migor.feedless.generated.types.BoolUpdateOperationsInput
import org.migor.feedless.generated.types.NullableUpdateOperationsInput
import org.migor.feedless.generated.types.StringUpdateOperationsInput
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

  private val corrId = "test"

  @Mock
  lateinit var userDAO: UserDAO

  @InjectMocks
  lateinit var userService: UserService

  lateinit var user: UserEntity
  lateinit var githubId: String
  lateinit var userId: UUID

  @BeforeEach
  fun setUp() {
    user = mock(UserEntity::class.java)
    githubId = "123678"
    userId = UUID.randomUUID()
    `when`(user.id).thenReturn(userId)
    `when`(user.githubId).thenReturn(githubId)
    `when`(user.email).thenReturn("$githubId@github.com ")
    `when`(userDAO.findById(any(UUID::class.java))).thenReturn(Optional.of(user))
  }

  @Test
  fun `updateLegacyUser updates email address`() {
    // when
    userService.updateLegacyUser(corrId, user, githubId)

    // then
    verify(user).email = "$userId@feedless.org"
    verify(user, times(0)).githubId = any(String::class.java)

    verify(userDAO).save(eq(user))
  }

  @Test
  fun `updating email defaults validatedEmailAt and hasValidatedEmail`() {
    val email = "test@feedless.org"
    val data = UpdateCurrentUserInput(
      email = StringUpdateOperationsInput(email),
    )
    userService.updateUser(corrId, UUID.randomUUID(), data)

    verify(user).email // read
    verify(user).email = email
    verify(user).validatedEmailAt = null
    verify(user).hasValidatedEmail = false
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

  @Test
  fun `accepting terms alters acceptedTermsAt`() {
    val data = UpdateCurrentUserInput(
      acceptedTermsAndServices = BoolUpdateOperationsInput(true),
    )
    userService.updateUser(corrId, UUID.randomUUID(), data)

    verify(user).hasAcceptedTerms = true
    verify(user).acceptedTermsAt = any(java.sql.Timestamp::class.java)
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))

  }

  @Test
  fun `rejecting terms alters acceptedTermsAt`() {
    val data = UpdateCurrentUserInput(
      acceptedTermsAndServices = BoolUpdateOperationsInput(false),
    )
    userService.updateUser(corrId, UUID.randomUUID(), data)

    verify(user).hasAcceptedTerms = false
    verify(user).acceptedTermsAt = null
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

  @Test
  fun `unsetting purgeScheduledFor will unset purgeScheduledFor`() {
    val data = UpdateCurrentUserInput(
      purgeScheduledFor = NullableUpdateOperationsInput(true),
    )
    userService.updateUser(corrId, UUID.randomUUID(), data)

    verify(user).purgeScheduledFor = null
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

   @Test
   fun `setting purgeScheduledFor will set purgeScheduledFor`() {
     val data = UpdateCurrentUserInput(
       purgeScheduledFor = NullableUpdateOperationsInput(false),
     )
     userService.updateUser(corrId, UUID.randomUUID(), data)

     verify(user).purgeScheduledFor = any(java.sql.Timestamp::class.java)
     verifyNoMoreInteractions(user)
     verify(userDAO).save(eq(user))
  }

  @Test
  fun `given updateUser is requested, fields will be altered`() {
    val firstName = "firstName"
    val lastName = "lastname"
    val country = "country"
    val data = UpdateCurrentUserInput(
      firstName = StringUpdateOperationsInput(firstName),
      lastName = StringUpdateOperationsInput(lastName),
      country = StringUpdateOperationsInput(country),
    )
    userService.updateUser(corrId, UUID.randomUUID(), data)

    verify(user).firstName = firstName
    verify(user).lastName = lastName
    verify(user).country = country
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

  @Test
  fun `given github id is not present, updateLegacyUser will set it`() {
    val user = mock(UserEntity::class.java)
    val githubId = "123678"
    `when`(user.email).thenReturn("")

    userService.updateLegacyUser(corrId, user, githubId)

    verify(user).githubId = githubId
    verify(user, times(0)).email = any(String::class.java)

    verify(userDAO).save(eq(user))
  }

}
