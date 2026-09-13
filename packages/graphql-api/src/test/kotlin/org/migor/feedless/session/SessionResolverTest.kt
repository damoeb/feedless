package org.migor.feedless.session

import com.netflix.graphql.dgs.context.DgsContext
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.util.JsonSerializer.toJson
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.time.LocalDateTime

class SessionResolverTest {

  private val userId = UserId()

  private val capabilityService = mock<CapabilityService> {
    on { hasCapability(UserCapability.ID) } doReturn true
    on { getCapability(UserCapability.ID) } doReturn UnresolvedCapability(UserCapability.ID, toJson(userId))
  }

  private val sessionTokenPort = mock<SessionTokenPort> {
    on { createExpiredTokenCookie(any()) } doReturn HttpSetCookie(name = "JSESSION", value = "", maxAge = 0)
  }

  // A DGS context without request data, as outside a web request.
  private val dfe = mock<DataFetchingEnvironment> {
    on { graphQlContext } doReturn GraphQLContext.newContext().also { DgsContext(null, null).accept(it) }.build()
  }

  @Test
  fun `a token for a user that no longer exists is not logged in`() = runTest {
    val userRepository = mock<UserRepository> { on { findById(any()) } doReturn null }

    val session = SessionResolver(sessionTokenPort, capabilityService, userRepository).session(dfe)

    assertThat(session.isLoggedIn).isFalse()
    assertThat(session.userId).isNull()
  }

  @Test
  fun `a token for an existing user is logged in`() = runTest {
    val user = User(id = userId, email = "someone@localhost", lastLogin = LocalDateTime.now(), hasAcceptedTerms = true)
    val userRepository = mock<UserRepository> { on { findById(any()) } doReturn user }

    val session = SessionResolver(sessionTokenPort, capabilityService, userRepository).session(dfe)

    assertThat(session.isLoggedIn).isTrue()
    assertThat(session.userId).isEqualTo(userId.uuid.toString())
  }
}
