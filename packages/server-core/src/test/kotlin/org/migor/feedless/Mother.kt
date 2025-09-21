package org.migor.feedless

import org.migor.feedless.document.DocumentId
import org.migor.feedless.group.GroupId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import org.migor.feedless.user.ConnectedAppId
import org.migor.feedless.user.UserId
import java.util.*

object Mother {
  fun randomUserId(): UserId = UserId(UUID.randomUUID())
  fun randomGroupId(): GroupId = GroupId(UUID.randomUUID())
  fun randomDocumentId(): DocumentId = DocumentId(UUID.randomUUID())
  fun randomRepositoryId(): RepositoryId = RepositoryId(UUID.randomUUID())
  fun randomSourceId(): SourceId = SourceId(UUID.randomUUID())
  fun randomConnectedAppId(): ConnectedAppId = ConnectedAppId(UUID.randomUUID())
}
