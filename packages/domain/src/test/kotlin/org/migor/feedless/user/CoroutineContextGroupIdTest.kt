package org.migor.feedless.user

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.group.GroupId
import org.migor.feedless.session.NoActingGroupException

/** Every group-scoped use case reads its group through `groupId()`, so a request without one fails cleanly. */
class CoroutineContextGroupIdTest {

  @Test
  fun `groupId answers the request's acting group`() {
    val groupId = GroupId()

    runTest(RequestContext(userId = UserId(), groupId = groupId)) {
      assertThat(currentCoroutineContext().groupId()).isEqualTo(groupId)
    }
  }

  @Test
  fun `groupId fails with NoActingGroupException when the request acts in no group`() {
    assertThatExceptionOfType(NoActingGroupException::class.java)
      .isThrownBy { runTest(RequestContext(userId = UserId())) { currentCoroutineContext().groupId() } }
      .withMessageContaining("Create a new token")
  }

  @Test
  fun `groupId fails with NoActingGroupException outside any request`() {
    assertThatExceptionOfType(NoActingGroupException::class.java)
      .isThrownBy { runTest { currentCoroutineContext().groupId() } }
  }
}
