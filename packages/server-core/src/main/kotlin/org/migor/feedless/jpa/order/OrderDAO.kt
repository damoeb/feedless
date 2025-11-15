package org.migor.feedless.jpa.order

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
interface OrderDAO : JpaRepository<OrderEntity, UUID> {
  fun findAllByUserId(userId: UUID, pageable: PageRequest): Page<OrderEntity>
}
