package org.migor.feedless.secrets

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.any2
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.group.GroupId
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.oauth2.jwt.Jwt
import kotlin.time.Duration.Companion.seconds

class UserSecretUseCaseTest {

  private lateinit var userSecretRepository: UserSecretRepository
  private lateinit var userRepository: UserRepository
  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var userSecretUseCase: UserSecretUseCase
  private lateinit var currentUserId: UserId
  private lateinit var currentUser: User

  @BeforeEach
  fun setUp() = runTest {
    currentUser = mock(User::class.java)
    currentUserId = randomUserId()
    `when`(currentUser.id).thenReturn(currentUserId)
    userSecretRepository = mock(UserSecretRepository::class.java)
    `when`(userSecretRepository.save(any2())).thenAnswer { it.arguments[0] }

    userRepository = mock(UserRepository::class.java)
    `when`(userRepository.findById(currentUserId)).thenReturn(mock(User::class.java))

    jwtTokenIssuer = mock(JwtTokenIssuer::class.java)
    val jwt = mock(Jwt::class.java)
    `when`(jwt.tokenValue).thenReturn("jwt")
    `when`(jwtTokenIssuer.createJwtForApi(any2())).thenReturn(jwt)
    `when`(jwtTokenIssuer.getExpiration(any2())).thenReturn(2.seconds)

    userSecretUseCase = UserSecretUseCase(userSecretRepository, userRepository, jwtTokenIssuer)

  }

  @Test
  fun `can create encrypted secret`() = runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
    userSecretUseCase.createUserSecret()

    verify(userSecretRepository).save(any2())
  }

  @Test
  fun `can create unencrypted secret`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      userSecretUseCase.createUserSecret()

      verify(userSecretRepository).save(any2())
    }


  @Test
  fun `others cannot delete his secret`() {
    val secret = mock(UserSecret::class.java)
    `when`(secret.ownerId).thenReturn(UserId())
    `when`(userSecretRepository.findById(any2())).thenReturn(secret)

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
        userSecretUseCase.deleteUserSecret(
          UserSecretId()
        )
      }
    }
  }
}
