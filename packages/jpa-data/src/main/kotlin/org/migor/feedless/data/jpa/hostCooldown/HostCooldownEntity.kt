package org.migor.feedless.data.jpa.hostCooldown

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.migor.feedless.hostCooldown.HostCooldownState
import java.time.LocalDateTime

@Entity
@Table(name = "t_host_cooldown")
open class HostCooldownEntity {

  @Id
  @Column(name = "host", nullable = false)
  open lateinit var host: String

  @Column(name = "blocked_until", nullable = false)
  open lateinit var blockedUntil: LocalDateTime

  @Column(name = "strikes", nullable = false)
  open var strikes: Int = 0

  @Column(name = "last_status")
  open var lastStatus: Int? = null

  @Column(name = "updated_at", nullable = false)
  open lateinit var updatedAt: LocalDateTime

  fun toDomain() = HostCooldownState(host, blockedUntil, strikes, lastStatus)
}
