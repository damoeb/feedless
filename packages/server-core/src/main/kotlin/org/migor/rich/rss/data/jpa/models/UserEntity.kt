package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.migor.rich.rss.data.jpa.EntityWithUUID
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "t_user")
open class UserEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false, unique = true)
  open lateinit var email: String

  @Basic
  @Column(nullable = false)
  open lateinit var name: String

  @Basic
  @Column(nullable = false)
  open lateinit var secretKey: String

  @Basic
  @Column(nullable = false)
  open var isRoot: Boolean = false

  @Basic
  @Column(nullable = false)
  open var hasApprovedTerms: Boolean = false

  @Basic
  open var approvedTermsAt: Timestamp? = null

  @Basic
  @Column(name = "date_format")
  open var dateFormat: String? = null

  @Basic
  @Column(name = "time_format", nullable = true)
  open var timeFormat: String? = null

  @Basic
  @Column(name = "notifications_stream_id", insertable = false, updatable = false, nullable = false)
  open var notificationsStreamId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(name = "notifications_stream_id", referencedColumnName = "id")
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

