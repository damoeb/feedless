package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import org.migor.rich.rss.util.JsonUtil
import java.util.*
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "t_user")
open class UserEntity : EntityWithUUID() {
  fun toJson(): String {
    return JsonUtil.gson.toJson(this)
  }

  @Basic
  @Column(name = "email")
  open var email: String? = null

  @Basic
  @Column(name = "name", unique = true)
  open var name: String? = null

  @Basic
  @Column(name = "date_format")
  open var dateFormat: String? = null

  @Basic
  @Column(name = "time_format", nullable = true)
  open var timeFormat: String? = null

  @Basic
  @Column(name = "notifications_stream_id", insertable = false, updatable = false)
  open var notificationsStreamId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(name = "stream_id", referencedColumnName = "id")
  open var notificationsStream: StreamEntity? = null

//  @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
//  @JoinTable(
//    name = "map_user_to_generic_feed",
//    joinColumns = [
//      JoinColumn(
//        name = "user_id", referencedColumnName = "id",
//        nullable = false, updatable = false
//      )],
//    inverseJoinColumns = [
//      JoinColumn(
//        name = "generic_feed_id", referencedColumnName = "id",
//        nullable = false, updatable = false
//      )
//    ]
//  )
//  open var genericFeeds: MutableList<GenericFeedEntity> = mutableListOf()
//  @PrePersist
//  fun prePersist() {
//    if (notificationsStreamId == null) {
//      notificationsStream = StreamEntity()
//    }
//  }
}

