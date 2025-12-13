package org.migor.feedless.data.jpa.connectedApp

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.connectedApp.ConnectedApp
import org.migor.feedless.connectedApp.ConnectedAppId
import org.migor.feedless.connectedApp.ConnectedAppRepository
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
class ConnectedAppJpaRepository(private val connectedAppDAO: ConnectedAppDAO) : ConnectedAppRepository {
  override fun findByIdAndAuthorizedEquals(
    id: ConnectedAppId,
    isAuthorized: Boolean
  ): ConnectedApp? {
    return connectedAppDAO.findByIdAndAuthorizedEquals(id.uuid, isAuthorized)?.toDomain()
  }

  override fun findAllByUserId(userId: UserId): List<ConnectedApp> {
    return connectedAppDAO.findAllByUserId(userId.uuid).map { it.toDomain() }
  }

  override fun findByIdAndAuthorizedEqualsAndUserIdIsNull(
    id: ConnectedAppId,
    isAuthorized: Boolean
  ): ConnectedApp? {
    return connectedAppDAO.findByIdAndAuthorizedEqualsAndUserIdIsNull(id.uuid, isAuthorized)?.toDomain()
  }

  override fun findByIdAndUserIdEquals(
    id: ConnectedAppId,
    userId: UserId
  ): ConnectedApp? {
    return connectedAppDAO.findByIdAndUserIdEquals(id.uuid, userId.uuid)?.toDomain()
  }

  override fun save(connectedApp: ConnectedApp): ConnectedApp {
    return connectedAppDAO.save(connectedApp.toEntity()).toDomain()
  }

  override fun deleteById(id: ConnectedAppId) {
    connectedAppDAO.deleteById(id.uuid)
  }
}
