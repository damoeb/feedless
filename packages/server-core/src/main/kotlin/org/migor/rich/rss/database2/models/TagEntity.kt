package org.migor.rich.rss.database2.models

import org.migor.rich.rss.database2.EntityWithUUID
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

@Entity
@Table(name = "t_tag")
class TagEntity() : EntityWithUUID() {

  @Basic
  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  open var type: TagType? = null

  @Basic
  @Column(name = "name", nullable = false, unique = true)
  open var name: String? = null

  constructor(type: TagType?, name: String?) : this() {
    this.type = type
    this.name = name
  }

}

enum class TagType {
  CONTENT, CUSTOM
}
