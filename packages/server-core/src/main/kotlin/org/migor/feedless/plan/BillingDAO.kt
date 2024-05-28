package org.migor.feedless.plan

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.saas)
interface BillingDAO : JpaRepository<BillingEntity, UUID> {
  fun findAllByUserId(userId: UUID, pageable: PageRequest): Page<BillingEntity>
}
