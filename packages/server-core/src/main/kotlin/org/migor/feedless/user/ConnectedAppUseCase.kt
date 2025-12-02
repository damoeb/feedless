package org.migor.feedless.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.connectedApp.ConnectedApp
import org.migor.feedless.connectedApp.ConnectedAppRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service


@Service
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class ConnectedAppUseCase(private val connectedAppRepository: ConnectedAppRepository) {

  suspend fun findAllByUserId(userId: UserId): List<ConnectedApp> = withContext(Dispatchers.IO) {
    connectedAppRepository.findAllByUserId(userId)
  }

}
