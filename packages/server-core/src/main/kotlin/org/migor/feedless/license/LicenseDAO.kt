package org.migor.feedless.license

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.saas)
interface LicenseDAO : JpaRepository<LicenseEntity, UUID> {
  fun findAllByOrderId(orderId: UUID): List<LicenseEntity>
}
