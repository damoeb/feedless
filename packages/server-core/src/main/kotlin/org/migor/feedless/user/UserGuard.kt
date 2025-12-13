package org.migor.feedless.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.guard.ResourceGuard
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class UserGuard(
  private val userRepository: UserRepository,
) : ResourceGuard<UserId, User> {


  override suspend fun requireRead(id: UserId): User = withContext(Dispatchers.IO) {
    val user = userRepository.findById(coroutineContext.userId()) ?: throw IllegalArgumentException("User not found")

    require(!user.anonymous, { "denied" })
    require(!user.banned, { "denied" })
    require(user.bannedUntil?.isBefore(LocalDateTime.now()) ?: true, { "denied" })
    user
  }


  override suspend fun requireWrite(id: UserId): User {
    TODO("Not yet implemented")
  }

  override suspend fun requireExecute(id: UserId): User {
    TODO("Not yet implemented")
  }

}
