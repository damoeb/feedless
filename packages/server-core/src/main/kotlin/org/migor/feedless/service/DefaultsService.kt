package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class DefaultsService {
  fun forHarvestItems(value: Boolean?) = value ?: false

}
