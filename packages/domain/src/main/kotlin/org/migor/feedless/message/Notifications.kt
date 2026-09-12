package org.migor.feedless.message

interface Notifications {
  fun showOptionsForKnownUser(chatId: Long)
  fun sendMessage(chatId: Long, message: String)
}
