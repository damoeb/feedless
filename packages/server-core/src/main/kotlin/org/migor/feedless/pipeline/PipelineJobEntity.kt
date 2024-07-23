package org.migor.feedless.pipeline

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Lob
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID
import java.util.*

enum class PipelineJobStatus {
  PENDING,
  IN_PROGRESS,
  FAILED,
  SUCCEEDED
}

@Entity
@Table(
  name = "t_pipeline_job"
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
  name = "type",
  discriminatorType = DiscriminatorType.STRING
)
open class PipelineJobEntity : EntityWithUUID() {

  @Column(name = "sequence_id", nullable = false)
  open var sequenceId: Int = -1

  @Column(name = "attempt", nullable = false)
  open var attempt: Int = 0

  @Column(name = "terminated_at")
  open var terminatedAt: Date? = null

  @Column(name = "terminated")
  open var terminated: Boolean = false

  @Column(name = "cool_down_until")
  open var coolDownUntil: Date? = null

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  open var status: PipelineJobStatus = PipelineJobStatus.PENDING

  @Lob
  @Column(name = "logs", columnDefinition = "TEXT")
  open var logs: String? = null

  fun updateStatus() {
    if (status != PipelineJobStatus.PENDING) {
      terminatedAt = Date()
      terminated = true
    }
  }
}
