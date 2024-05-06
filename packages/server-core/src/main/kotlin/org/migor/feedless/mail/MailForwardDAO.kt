package org.migor.feedless.mail

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface MailForwardDAO : JpaRepository<MailForwardEntity, UUID> {
  fun findAllByRepositoryId(id: UUID): List<MailForwardEntity>

}
