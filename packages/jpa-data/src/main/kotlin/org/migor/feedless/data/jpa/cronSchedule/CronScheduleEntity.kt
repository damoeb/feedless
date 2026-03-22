package org.migor.feedless.data.jpa.cronSchedule

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.Size
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.document.DocumentEntity.Companion.LEN_STR_DEFAULT
import java.time.LocalDateTime

@Entity
@Table(name = "t_cron_schedule")
open class CronScheduleEntity : EntityWithUUID() {

  @Size(message = "cronExpression", max = LEN_STR_DEFAULT)
  @Column(nullable = false, name = "scheduler_expression")
  open lateinit var cronExpression: String

  @Column(name = "scheduled_next_at")
  open var scheduledNextAt: LocalDateTime? = null
}


fun CronScheduleEntity.toDomain(): CronSchedule {
  return CronScheduleMapper.INSTANCE.toDomain(this)
}

fun CronSchedule.toEntity(): CronScheduleEntity {
  return CronScheduleMapper.Companion.INSTANCE.toEntity(this)
}
