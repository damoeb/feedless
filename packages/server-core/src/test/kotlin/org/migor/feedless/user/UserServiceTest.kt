package org.migor.feedless.user

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.document.any
import org.migor.feedless.document.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
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

  @Test
  fun `updateLegacyUser updates email address`() {
    val user = mock(UserEntity::class.java)
    val githubId = "123678"
    val userId = UUID.randomUUID()
    `when`(user.id).thenReturn(userId)
    `when`(user.githubId).thenReturn(githubId)
    `when`(user.email).thenReturn("$githubId@github.com ")

    userService.updateLegacyUser(corrId, user, githubId)

    verify(user).email = "$userId@feedless.org"
    verify(user, times(0)).githubId = any(String::class.java)

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
