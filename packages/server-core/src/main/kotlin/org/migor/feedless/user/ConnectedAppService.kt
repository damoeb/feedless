package org.migor.feedless.user

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.connectedApp.ConnectedApp
import org.migor.feedless.connectedApp.ConnectedAppRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class ConnectedAppService(private val connectedAppRepository: ConnectedAppRepository) {

  @Transactional(readOnly = true)
  suspend fun findAllByUserId(userId: UserId): List<ConnectedApp> {
    return connectedAppRepository.findAllByUserId(userId)
  }

}
