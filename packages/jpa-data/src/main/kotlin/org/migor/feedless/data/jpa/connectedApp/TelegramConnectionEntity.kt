package org.migor.feedless.data.jpa.connectedApp

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("telegram")
open class TelegramConnectionEntity : ConnectedAppEntity() {
  @Column(name = "chat_id")
  open var chatId: Long = 0
}

fun TelegramConnectionEntity.toDomain(): org.migor.feedless.connectedApp.TelegramConnection {
  return TelegramConnectionMapper.Companion.INSTANCE.toDomain(this)
}
