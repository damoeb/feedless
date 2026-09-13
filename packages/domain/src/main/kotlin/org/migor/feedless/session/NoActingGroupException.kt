package org.migor.feedless.session

import org.migor.feedless.user.UserId

/** The user owns no group the token could act in, e.g. an old token or one for a group they've left. 403 NO_ACTING_GROUP. */
class NoActingGroupException(message: String) : RuntimeException(message) {
  companion object {
    fun forIssuance(userId: UserId) =
      NoActingGroupException("user ${userId.uuid} owns no group, so no token can be issued for it")

    fun forRequest() =
      NoActingGroupException("This token acts in no group you own. Create a new token to act in your group.")
  }
}
