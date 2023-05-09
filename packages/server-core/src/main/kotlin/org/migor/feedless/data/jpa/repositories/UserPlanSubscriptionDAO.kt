package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.data.jpa.models.UserPlanSubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserPlanSubscriptionDAO : JpaRepository<UserPlanSubscriptionEntity, UUID>
