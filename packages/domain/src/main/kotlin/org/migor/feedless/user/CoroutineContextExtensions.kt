package org.migor.feedless.user

import org.migor.feedless.capability.RequestContext
import org.migor.feedless.group.GroupId
import org.migor.feedless.session.NoActingGroupException
import kotlin.coroutines.CoroutineContext

fun CoroutineContext.corrId(): String? {
  return this[RequestContext]?.corrId
}

fun CoroutineContext.userId(): UserId {
  return this[RequestContext]?.userId!!
}

fun CoroutineContext.userIdMaybe(): UserId? {
  return this[RequestContext]?.userId
}

/** @throws NoActingGroupException if the token carries no group, or one the user no longer owns. */
fun CoroutineContext.groupId(): GroupId {
  return this[RequestContext]?.groupId ?: throw NoActingGroupException.forRequest()
}

fun CoroutineContext.isAdmin(): Boolean {
  return this[RequestContext]?.isAdmin ?: false
}
