package org.migor.feedless.secrets

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.repository.any
import org.migor.feedless.repository.any2
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.user.UserEntity
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.oauth2.jwt.Jwt
import java.util.*

class UserSecretServiceTest {

  private lateinit var userSecretDAO: UserSecretDAO
  private lateinit var tokenProvider: TokenProvider
  private lateinit var userSecretService: UserSecretService
  private lateinit var currentUserId: UUID
  private lateinit var currentUser: UserEntity

  @BeforeEach
  fun setUp() = runTest {
    currentUser = mock(UserEntity::class.java)
    currentUserId = UUID.randomUUID()
    `when`(currentUser.id).thenReturn(currentUserId)
    userSecretDAO = mock(UserSecretDAO::class.java)
    `when`(userSecretDAO.save(any2())).thenAnswer { it.arguments[0] }


    tokenProvider = mock(TokenProvider::class.java)
    val jwt = mock(Jwt::class.java)
    `when`(jwt.tokenValue).thenReturn("jwt")
    `when`(tokenProvider.createJwtForApi(any2())).thenReturn(jwt)

    userSecretService = UserSecretService(userSecretDAO, tokenProvider)

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
