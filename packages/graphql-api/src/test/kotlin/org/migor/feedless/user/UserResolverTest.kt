package org.migor.feedless.user

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.migor.feedless.generated.types.Session as SessionDto

class UserResolverTest {

  @Test
  fun `a session whose user no longer exists has no user`() = runTest {
    val userRepository = mock<UserRepository> { on { findById(any()) } doReturn null }
    val resolver = UserResolver(mock(), userRepository, mock(), mock())
    val session = SessionDto(isLoggedIn = true, isAnonymous = false, userId = UserId().uuid.toString())
    val dfe = mock<DgsDataFetchingEnvironment> { on { getSourceOrThrow<SessionDto>() } doReturn session }

    assertThat(resolver.getUserForSession(dfe)).isNull()
  }
}
