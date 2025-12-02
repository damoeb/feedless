package org.migor.feedless.connectedApp

import org.migor.feedless.user.UserId

interface ConnectedAppRepository {
  fun findByIdAndAuthorizedEquals(id: ConnectedAppId, isAuthorized: Boolean): ConnectedApp?
  fun findAllByUserId(userId: UserId): List<ConnectedApp>
  fun findByIdAndAuthorizedEqualsAndUserIdIsNull(id: ConnectedAppId, isAuthorized: Boolean): ConnectedApp?
  fun findByIdAndUserIdEquals(id: ConnectedAppId, userId: UserId): ConnectedApp?
  fun save(connectedApp: ConnectedApp): ConnectedApp
  fun deleteById(id: ConnectedAppId)
}
