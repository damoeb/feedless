package org.migor.feedless.capability

import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.util.CryptUtil.newCorrId
import kotlin.coroutines.CoroutineContext

data class RequestContext(
  val corrId: String? = newCorrId(),
  val isAdmin: Boolean? = false,
  var userId: UserId? = null,
  var groupId: GroupId? = null
) : CoroutineContext.Element {
  companion object Key : CoroutineContext.Key<RequestContext>

  override val key: CoroutineContext.Key<*> = Key
}
