package org.migor.feedless.jpa.user

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.migor.feedless.jpa.connectedApp.ConnectedAppEntity

@Entity
@DiscriminatorValue("telegram")
open class TelegramConnectionEntity : ConnectedAppEntity() {
  @Column(name = "chat_id")
  open var chatId: Long = 0
}
