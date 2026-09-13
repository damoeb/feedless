package org.migor.feedless.capability

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.slf4j.MDC

class RequestContextMdcTest {

  private val userId = UserId()
  private val groupId = GroupId()

  @BeforeEach
  @AfterEach
  fun clearMdc() {
    MDC.clear()
  }

  @Test
  fun `the MDC follows the coroutine onto IO threads and into async children`() {
    val seen = runBlocking(RequestContext(corrId = "c1", userId = userId, groupId = groupId)) {
      val onIo = withContext(Dispatchers.IO) { mdcSnapshot() }
      val inAsync = async(Dispatchers.Default) { mdcSnapshot() }.await()
      listOf(mdcSnapshot(), onIo, inAsync)
    }

    val expected = mapOf(
      MdcKeys.CORR_ID to "c1",
      MdcKeys.USER_ID to userId.uuid.toString(),
      MdcKeys.GROUP_ID to groupId.uuid.toString(),
    )
    assertThat(seen).containsOnly(expected)
  }

  @Test
  fun `leaving the context restores the thread's previous MDC`() {
    MDC.put(MdcKeys.CORR_ID, "outer")
    MDC.put("other", "x")

    runBlocking(RequestContext(corrId = "c1", userId = userId)) {
      assertThat(MDC.get(MdcKeys.CORR_ID)).isEqualTo("c1")
    }

    assertThat(MDC.getCopyOfContextMap()).isEqualTo(mapOf(MdcKeys.CORR_ID to "outer", "other" to "x"))
  }

  @Test
  fun `constructing a context leaves the MDC alone`() {
    RequestContext(corrId = "c1", userId = userId)

    assertThat(MDC.get(MdcKeys.CORR_ID)).isNull()
  }

  private fun mdcSnapshot(): Map<String, String> = MDC.getCopyOfContextMap().orEmpty()
}
