package org.migor.rich.rss.database2.models

import org.migor.rich.rss.database2.EntityWithUUID
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name="t_user")
open class UserEntity: EntityWithUUID() {

  @Basic
  @Column(name = "email")
  open var email: String? = null

  @Basic
  @Column(name = "name")
  open var name: String? = null

  @Basic
  @Column(name = "date_format")
  open var dateFormat: String? = null

  @Basic
  @Column(name = "time_format", nullable = true)
  open var timeFormat: String? = null

  @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
  @JoinTable(
    name = "map_user_to_generic_feed",
    joinColumns = [
      JoinColumn(
        name = "user_id", referencedColumnName = "id",
        nullable = false, updatable = false
      )],
    inverseJoinColumns = [
      JoinColumn(
        name = "generic_feed_id", referencedColumnName = "id",
        nullable = false, updatable = false
      )
    ]
  )
  open var refFeedEntities: MutableList<GenericFeedEntity> = mutableListOf()

}

