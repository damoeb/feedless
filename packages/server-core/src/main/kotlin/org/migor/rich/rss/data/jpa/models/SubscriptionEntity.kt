package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.migor.rich.rss.data.jpa.EntityWithUUID
import java.util.*

@Entity
@Table(name = "t_subscription")
open class SubscriptionEntity : EntityWithUUID() {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bucketId", referencedColumnName = "id")
  open var bucket: BucketEntity? = null

  @Basic
  open var filter: String? = null

  @Basic
  @Column(name = "bucketId", insertable = false, updatable = false)
  open var bucketId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feedId", referencedColumnName = "id")
  open var feed: NativeFeedEntity? = null

  @Basic
  @Column(name = "feedId", insertable = false, updatable = false)
  open var feedId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userId", referencedColumnName = "id", insertable = false, updatable = false)
  open var user: UserEntity? = null

  @Basic
  @Column(name = "userId", nullable = false)
  open lateinit var userId: UUID

  @PrePersist
  fun prePersist() {
    val withFeed = feed == null || feedId == null
    val withBucket = bucket == null || bucketId == null
    if (withFeed && withBucket) {
      throw IllegalArgumentException("either feed or bucket, not both")
    }
    if (!withFeed && !withBucket) {
      throw IllegalArgumentException("feed or bucket must be set")
    }
  }
}

