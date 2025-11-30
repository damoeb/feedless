package org.migor.feedless.secrets

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.any2
import org.migor.feedless.data.jpa.userSecret.UserSecretDAO
import org.migor.feedless.data.jpa.userSecret.UserSecretEntity
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.oauth2.jwt.Jwt
import java.util.*
import kotlin.time.Duration.Companion.seconds

class UserSecretServiceTest {

  private lateinit var userSecretDAO: UserSecretDAO
  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var userSecretService: UserSecretService
  private lateinit var currentUserId: UserId
  private lateinit var currentUser: User

  @BeforeEach
  fun setUp() = runTest {
    currentUser = mock(User::class.java)
    currentUserId = randomUserId()
    `when`(currentUser.id).thenReturn(currentUserId)
    userSecretDAO = mock(UserSecretDAO::class.java)
    `when`(userSecretDAO.save(any2())).thenAnswer { it.arguments[0] }

    jwtTokenIssuer = mock(JwtTokenIssuer::class.java)
    val jwt = mock(Jwt::class.java)
    `when`(jwt.tokenValue).thenReturn("jwt")
    `when`(jwtTokenIssuer.createJwtForApi(any2())).thenReturn(jwt)
    `when`(jwtTokenIssuer.getExpiration(any2())).thenReturn(2.seconds)

    userSecretService = UserSecretService(userSecretDAO, jwtTokenIssuer)

  }

  @Test
  fun `can create encrypted secret`() = runTest {
    userSecretService.createUserSecret(currentUser)

    verify(userSecretDAO).save(any2())
  }

  @Test
  fun `can create unencrypted secret`() = runTest {
    userSecretService.createUserSecret(currentUser)

    verify(userSecretDAO).save(any2())
  }


  @Test
  fun `others cannot delete his secret`() {
    val secret = mock(UserSecretEntity::class.java)
    `when`(secret.ownerId).thenReturn(UUID.randomUUID())
    `when`(userSecretDAO.findById(any2())).thenReturn(Optional.of(secret))

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest {
        userSecretService.deleteUserSecret(
          currentUser, UUID.randomUUID()
        )
      }
    }
  }
}
