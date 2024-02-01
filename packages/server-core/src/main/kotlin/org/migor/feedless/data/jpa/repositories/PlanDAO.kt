package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.PlanAvailability
import org.migor.feedless.data.jpa.models.PlanEntity
import org.migor.feedless.data.jpa.models.PlanName
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface PlanDAO : JpaRepository<PlanEntity, UUID> {
  fun findAllByAvailabilityNotAndProduct(availability: PlanAvailability, product: ProductName): List<PlanEntity>
  fun findByNameAndProduct(name: PlanName, product: ProductName): PlanEntity?
}
