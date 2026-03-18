package org.migor.feedless.karma

import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.springframework.stereotype.Component

@Component
class KarmaHandlerFactory(
  private val userRepository: UserRepository,
  private val karmaChangeRepository: KarmaChangeRepository
) : HandlerFactory<UserId, UserKarmaHandler> {
  override fun from(userId: UserId): UserKarmaHandler {
    return UserKarmaHandler(userRepository, karmaChangeRepository, userId)
  }

}
