package org.migor.feedless.capability

import kotlinx.coroutines.currentCoroutineContext
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.MDC
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

/** Servlet request attribute holding the request's correlation id. */
const val CORR_ID_REQUEST_ATTR = "corrId"

/** The current servlet request's id, else the thread's MDC id. */
fun currentThreadCorrId(): String? =
  // A completed request left on the thread throws instead of answering.
  runCatching {
    RequestContextHolder.getRequestAttributes()
      ?.getAttribute(CORR_ID_REQUEST_ATTR, RequestAttributes.SCOPE_REQUEST) as? String
  }.getOrNull() ?: MDC.get(MdcKeys.CORR_ID)

/** The id of the running request or job: the coroutine's context first, then the thread. */
suspend fun currentCorrId(): String? = currentCoroutineContext()[RequestContext]?.corrId ?: currentThreadCorrId()

/** Acts as [userId]/[groupId] within the current request or job, under its id. */
suspend fun inheritRequestContext(userId: UserId?, groupId: GroupId?): RequestContext =
  RequestContext(corrId = currentCorrId() ?: newCorrId(), userId = userId, groupId = groupId)

/** One fanned-out sub-task: `parent/child`, so siblings are told apart but trace back to the parent. */
suspend fun childRequestContext(userId: UserId?, groupId: GroupId?): RequestContext =
  RequestContext(corrId = newCorrId(parentCorrId = currentCorrId()), userId = userId, groupId = groupId)

/** Runs a blocking [block] with [corrId] in the MDC, then restores the thread's previous id. */
fun <T> withMdcCorrId(corrId: String = newCorrId(), block: (String) -> T): T {
  val previous = MDC.get(MdcKeys.CORR_ID)
  MDC.put(MdcKeys.CORR_ID, corrId)
  try {
    return block(corrId)
  } finally {
    if (previous == null) {
      MDC.remove(MdcKeys.CORR_ID)
    } else {
      MDC.put(MdcKeys.CORR_ID, previous)
    }
  }
}
