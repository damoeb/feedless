package org.migor.feedless.capability

import kotlinx.coroutines.ThreadContextElement
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.MDC
import kotlin.coroutines.CoroutineContext

object MdcKeys {
  const val CORR_ID = "corrId"
  const val USER_ID = "userId"
  const val GROUP_ID = "groupId"
}

/** Servlet request attribute key for [RequestContext] set by the HTTP API JWT filter. */
const val HTTP_API_REQUEST_CONTEXT_ATTR = "org.migor.feedless.http.requestContext"

/** Puts its ids into the MDC of whichever thread runs the coroutine, and restores that thread's MDC afterwards. */
data class RequestContext(
  val corrId: String = newCorrId(),
  val isAdmin: Boolean? = false, // todo not yet resolved
  var userId: UserId? = null,
  var groupId: GroupId? = null
) : ThreadContextElement<Map<String, String>?> {
  companion object Key : CoroutineContext.Key<RequestContext>

  override val key: CoroutineContext.Key<*> = Key

  override fun updateThreadContext(context: CoroutineContext): Map<String, String>? {
    val previous = MDC.getCopyOfContextMap()
    MDC.put(MdcKeys.CORR_ID, corrId)
    putOrRemove(MdcKeys.USER_ID, userId?.uuid?.toString())
    putOrRemove(MdcKeys.GROUP_ID, groupId?.uuid?.toString())
    return previous
  }

  override fun restoreThreadContext(context: CoroutineContext, oldState: Map<String, String>?) {
    if (oldState == null) {
      MDC.clear()
    } else {
      MDC.setContextMap(oldState)
    }
  }

  // A context without a user must not log the user of whatever ran on the thread before.
  private fun putOrRemove(key: String, value: String?) {
    if (value == null) {
      MDC.remove(key)
    } else {
      MDC.put(key, value)
    }
  }
}
