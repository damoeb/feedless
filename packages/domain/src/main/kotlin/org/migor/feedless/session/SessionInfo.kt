package org.migor.feedless.session

import org.migor.feedless.user.UserId

data class SessionInfo(
  val isLoggedIn: Boolean,
  val isAnonymous: Boolean,
  val userId: UserId? = null,
)
