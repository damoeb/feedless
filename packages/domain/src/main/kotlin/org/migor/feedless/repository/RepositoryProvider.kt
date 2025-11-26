package org.migor.feedless.repository

import org.migor.feedless.PageableRequest
import org.migor.feedless.capability.CapabilityConsumer
import org.migor.feedless.capability.UnresolvedCapability

interface RepositoryProvider : CapabilityConsumer {
  suspend fun provideAll(
    capability: UnresolvedCapability, pageable: PageableRequest,
    where: RepositoriesFilter?,
  ): List<Repository>
}
