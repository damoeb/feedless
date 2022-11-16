package org.migor.rich.rss.database

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.springframework.data.annotation.CreatedDate
import java.sql.Timestamp
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.MappedSuperclass


@MappedSuperclass
@TypeDefs(value = [TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)])
open class EntityWithUUID() {
  constructor(id: UUID) : this() {
    this.id = id
  }

  @Id
  @Type(type = "pg-uuid")
  open var id: UUID = UUID.randomUUID()

  @Basic
  @CreatedDate
  @Column(name = "createdAt", nullable = false)
  open var createdAt: Timestamp = Timestamp(System.currentTimeMillis())
}
