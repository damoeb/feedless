package org.migor.feedless.capability

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.slf4j.MDC
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

class CorrelationIdTest {

  private val userId = UserId()
  private val groupId = GroupId()

  @BeforeEach
  @AfterEach
  fun clean() {
    MDC.clear()
    RequestContextHolder.resetRequestAttributes()
  }

  @Test
  fun `a child keeps the parent's id as parent-slash-child`() {
    runBlocking(RequestContext(corrId = "p")) {
      val first = childRequestContext(userId, groupId)
      val second = childRequestContext(userId, groupId)

      assertThat(first.corrId).matches("p/[a-zA-Z0-9]{4}")
      assertThat(second.corrId).startsWith("p/").isNotEqualTo(first.corrId)
      assertThat(first.userId).isEqualTo(userId)
      assertThat(first.groupId).isEqualTo(groupId)
      withContext(first) {
        assertThat(MDC.get(MdcKeys.CORR_ID)).isEqualTo(first.corrId)
        assertThat(MDC.get(MdcKeys.USER_ID)).isEqualTo(userId.uuid.toString())
      }
    }
  }

  @Test
  fun `an inherited context keeps the parent's id unchanged`() {
    val inherited = runBlocking(RequestContext(corrId = "p")) { inheritRequestContext(userId, groupId) }

    assertThat(inherited.corrId).isEqualTo("p")
    assertThat(inherited.userId).isEqualTo(userId)
    assertThat(inherited.groupId).isEqualTo(groupId)
  }

  @Test
  fun `without a coroutine context the thread's MDC id is the parent`() {
    MDC.put(MdcKeys.CORR_ID, "m")

    val inherited = runBlocking { inheritRequestContext(userId, groupId) }
    val child = runBlocking { childRequestContext(userId, groupId) }

    assertThat(inherited.corrId).isEqualTo("m")
    assertThat(child.corrId).startsWith("m/")
  }

  @Test
  fun `the servlet request's id wins over the MDC`() {
    MDC.put(MdcKeys.CORR_ID, "m")
    RequestContextHolder.setRequestAttributes(requestWithCorrId("req-1"))

    assertThat(currentThreadCorrId()).isEqualTo("req-1")
  }

  @Test
  fun `withMdcCorrId sets the id for the block and restores the previous one`() {
    MDC.put(MdcKeys.CORR_ID, "outer")

    val seen = withMdcCorrId("job") { corrId -> corrId to MDC.get(MdcKeys.CORR_ID) }

    assertThat(seen).isEqualTo("job" to "job")
    assertThat(MDC.get(MdcKeys.CORR_ID)).isEqualTo("outer")
  }

  @Test
  fun `withMdcCorrId generates an id and removes it afterwards`() {
    val seen = withMdcCorrId { MDC.get(MdcKeys.CORR_ID) }

    assertThat(seen).isNotBlank()
    assertThat(MDC.get(MdcKeys.CORR_ID)).isNull()
  }

  private fun requestWithCorrId(corrId: String): RequestAttributes = mock {
    on { getAttribute(CORR_ID_REQUEST_ATTR, RequestAttributes.SCOPE_REQUEST) } doReturn corrId
  }
}
