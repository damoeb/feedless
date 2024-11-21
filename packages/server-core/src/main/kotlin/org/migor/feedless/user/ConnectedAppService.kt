package org.migor.feedless.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class ConnectedAppService {

  @Autowired
  lateinit var connectedAppDAO: ConnectedAppDAO

  @Transactional(readOnly = true)
  suspend fun findAllByUserId(userId: UUID): List<ConnectedAppEntity> {
    return withContext(Dispatchers.IO) {
      connectedAppDAO.findAllByUserId(userId)
    }
  }

}
