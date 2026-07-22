package org.migor.feedless.auth

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.group.GroupAssignmentSummary
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.session.AuthCredentialsException
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.whenever

class AuthUseCaseTest {

  private val currentUserId = UserId()
  private lateinit var userRepository: UserRepository
  private lateinit var groupUseCasePort: GroupUseCasePort
  private lateinit var authUseCase: AuthUseCase
  private lateinit var currentUser: User

  @BeforeEach
  fun setUp() {
    currentUser = mock(User::class.java)
    `when`(currentUser.id).thenReturn(currentUserId)
    `when`(currentUser.email).thenReturn("user@example.com")

    userRepository = mock(UserRepository::class.java)
    groupUseCasePort = mock(GroupUseCasePort::class.java)

    authUseCase = AuthUseCase(userRepository, groupUseCasePort)
  }

  @Test
  fun `currentUser returns id, email and groups for the authenticated user`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      whenever(userRepository.findById(currentUserId)).thenReturn(currentUser)
      val assignment = GroupAssignmentSummary(groupId = GroupId(), role = RoleInGroup.owner, name = "team")
      whenever(groupUseCasePort.listAssignments()).thenReturn(listOf(assignment))

      val result = authUseCase.currentUser()

      assertThat(result.id).isEqualTo(currentUserId)
      assertThat(result.email).isEqualTo("user@example.com")
      assertThat(result.groups).containsExactly(assignment)
    }

  @Test
  fun `currentUser throws AuthCredentialsException when no user is in context`() {
    assertThatExceptionOfType(AuthCredentialsException::class.java).isThrownBy {
      runBlocking {
        authUseCase.currentUser()
      }
    }
  }

  @Test
  fun `currentUser throws AuthCredentialsException when user record is missing`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      whenever(userRepository.findById(currentUserId)).thenReturn(null)

      assertThatExceptionOfType(AuthCredentialsException::class.java).isThrownBy {
        runBlocking(RequestContext(groupId = GroupId(), userId = currentUserId)) {
          authUseCase.currentUser()
        }
      }
    }
}
