package org.migor.feedless.capability

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

data class RequestContext(
  val corrId: String = newCorrId(),
  val isAdmin: Boolean? = false, // todo not yet resolved
  var userId: UserId? = null,
  var groupId: GroupId? = null
) : CoroutineContext.Element {
  companion object Key : CoroutineContext.Key<RequestContext>

  override val key: CoroutineContext.Key<*> = Key

  init {
    MDC.put(MdcKeys.CORR_ID, corrId)
    userId?.let { MDC.put(MdcKeys.USER_ID, it.uuid.toString()) }
    groupId?.let { MDC.put(MdcKeys.GROUP_ID, it.uuid.toString()) }
  }
}
