package org.migor.feedless.connectedApp

import org.migor.feedless.user.UserId

interface ConnectedAppRepository {
  suspend fun findByIdAndAuthorizedEquals(id: ConnectedAppId, isAuthorized: Boolean): ConnectedApp?
  suspend fun findAllByUserId(userId: UserId): List<ConnectedApp>
  suspend fun findByIdAndAuthorizedEqualsAndUserIdIsNull(id: ConnectedAppId, isAuthorized: Boolean): ConnectedApp?
  suspend fun findByIdAndUserIdEquals(id: ConnectedAppId, userId: UserId): ConnectedApp?
  suspend fun save(connectedApp: ConnectedApp): ConnectedApp
  suspend fun deleteById(id: ConnectedAppId)
}
