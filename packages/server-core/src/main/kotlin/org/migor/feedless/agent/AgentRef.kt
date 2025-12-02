package org.migor.feedless.agent

import kotlinx.coroutines.channels.Channel
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.OsInfo
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime

data class AgentRef(
  val secretKeyId: UserSecretId,
  val ownerId: UserId,
  val name: String,
  val version: String,
  val connectionId: String,
  val os: OsInfo,
  val addedAt: LocalDateTime,
  val emitter: Channel<AgentEvent>
) {
  override fun toString(): String {
    return "AgentRef(connectionId=$connectionId, secretKeyId=$secretKeyId, name=$name, version='$version', os=$os)"
  }
}
