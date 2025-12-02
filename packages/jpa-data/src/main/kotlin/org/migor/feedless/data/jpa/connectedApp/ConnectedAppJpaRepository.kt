package org.migor.feedless.data.jpa.connectedApp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
  override suspend fun findByIdAndAuthorizedEquals(
    id: ConnectedAppId,
    isAuthorized: Boolean
  ): ConnectedApp? {
    return withContext(Dispatchers.IO) {
      connectedAppDAO.findByIdAndAuthorizedEquals(id.uuid, isAuthorized)?.toDomain()
    }
  }

  override suspend fun findAllByUserId(userId: UserId): List<ConnectedApp> {
    return withContext(Dispatchers.IO) {
      connectedAppDAO.findAllByUserId(userId.uuid).map { it.toDomain() }
    }
  }

  override suspend fun findByIdAndAuthorizedEqualsAndUserIdIsNull(
    id: ConnectedAppId,
    isAuthorized: Boolean
  ): ConnectedApp? {
    return withContext(Dispatchers.IO) {
      connectedAppDAO.findByIdAndAuthorizedEqualsAndUserIdIsNull(id.uuid, isAuthorized)?.toDomain()
    }
  }

  override suspend fun findByIdAndUserIdEquals(
    id: ConnectedAppId,
    userId: UserId
  ): ConnectedApp? {
    return withContext(Dispatchers.IO) {
      connectedAppDAO.findByIdAndUserIdEquals(id.uuid, userId.uuid)?.toDomain()
    }
  }

  override suspend fun save(connectedApp: ConnectedApp): ConnectedApp {
    return withContext(Dispatchers.IO) {
      connectedAppDAO.save(connectedApp.toEntity()).toDomain()
    }

  }

  override suspend fun deleteById(id: ConnectedAppId) {
    withContext(Dispatchers.IO) {
      connectedAppDAO.deleteById(id.uuid)
    }
  }
}
