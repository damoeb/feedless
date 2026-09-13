package org.migor.feedless.message

import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.user.UserId

interface Notifications {
  fun showOptionsForKnownUser(chatId: Long)
  fun sendMessage(chatId: Long, message: String)

  /** Pushes items to the owner's authorized Telegram chat; no-op when Telegram is off or unlinked. */
  suspend fun pushToOwner(ownerId: UserId, items: List<JsonItem>)
}
