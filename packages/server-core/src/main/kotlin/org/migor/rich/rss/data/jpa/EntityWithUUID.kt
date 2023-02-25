package org.migor.rich.rss.data.jpa

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import java.sql.Timestamp
import java.util.*


@MappedSuperclass
open class EntityWithUUID() {
  constructor(id: UUID) : this() {
    this.id = id
  }

  @Id
  open var id: UUID = UUID.randomUUID()

  @Basic
  @CreatedDate
  @Column(name = "createdAt", nullable = false)
  open var createdAt: Timestamp = Timestamp(System.currentTimeMillis())
}
