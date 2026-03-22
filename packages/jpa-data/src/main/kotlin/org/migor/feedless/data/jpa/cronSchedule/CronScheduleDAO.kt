package org.migor.feedless.data.jpa.cronSchedule

import org.migor.feedless.AppLayer
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppLayer.repository)
interface CronScheduleDAO : JpaRepository<CronScheduleEntity, UUID>
