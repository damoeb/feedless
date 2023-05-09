package org.migor.feedless.data.jpa

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
  @Column(name = StandardJpaFields.id)
  open var id: UUID = UUID.randomUUID()

  @Basic
  @CreatedDate
  @Column(name = StandardJpaFields.createdAt, nullable = false)
  open var createdAt: Timestamp = Timestamp(System.currentTimeMillis())
}
