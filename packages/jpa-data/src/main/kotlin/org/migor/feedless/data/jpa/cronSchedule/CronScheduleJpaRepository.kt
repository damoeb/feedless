package org.migor.feedless.data.jpa.cronSchedule

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.cronSchedule.CronScheduleRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.document} & ${AppLayer.repository}")
class CronScheduleJpaRepository(private val cronScheduleDAO: CronScheduleDAO) :
  CronScheduleRepository {
  override fun save(cronSchedule: CronSchedule) {
    cronScheduleDAO.save(cronSchedule.toEntity())
  }
  
}
