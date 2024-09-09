package org.migor.feedless.secrets

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface OneTimePasswordDAO : JpaRepository<OneTimePasswordEntity, UUID> {
  fun deleteAllByValidUntilBefore(now: LocalDateTime)
}
