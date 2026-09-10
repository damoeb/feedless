package org.migor.feedless.session

import org.migor.feedless.user.UserId

/**
 * There is no group the user owns to act in. Raised when a token would be issued for a user who owns
 * no group, and when a request needs its acting group but carries none the user still owns: a token
 * issued before tokens carried a group, or one whose group the user has since been removed from or
 * demoted in. Mapped to 403 `NO_ACTING_GROUP` on `/api/v1`.
 */
class NoActingGroupException(message: String) : RuntimeException(message) {
  companion object {
    fun forIssuance(userId: UserId) =
      NoActingGroupException("user ${userId.uuid} owns no group, so no token can be issued for it")

    fun forRequest() =
      NoActingGroupException("This token acts in no group you own. Create a new token to act in your group.")
  }
}
