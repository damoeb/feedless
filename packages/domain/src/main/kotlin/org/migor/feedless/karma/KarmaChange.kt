package org.migor.feedless.karma

import org.migor.feedless.document.DocumentId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class KarmaChange(
  val id: KarmaChangeId = KarmaChangeId(),
  val action: KarmaChangeReason,
  val documentId: DocumentId,
  val userId: UserId,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)

