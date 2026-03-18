package org.migor.feedless.karma

import org.migor.feedless.document.DocumentId
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository

class UserKarmaHandler(
  private val userRepository: UserRepository,
  private val karmaChangeRepository: KarmaChangeRepository,
  private val userId: UserId
) {
  fun changeKarma(karmaChange: Int, reason: KarmaChangeReason, documentId: DocumentId) {
    val user = userRepository.findById(userId) ?: throw IllegalStateException("User with id $userId not found")

    userRepository.save(user.copy(karma = user.karma + karmaChange))

    karmaChangeRepository.append(KarmaChange(action = reason, userId = userId, documentId = documentId))
  }

}
