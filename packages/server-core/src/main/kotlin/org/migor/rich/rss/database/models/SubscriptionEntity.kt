package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.PrePersist
import javax.persistence.Table

@Entity
@Table(name = "t_subscription")
open class SubscriptionEntity : EntityWithUUID() {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bucketId", referencedColumnName = "id")
  open var bucket: BucketEntity? = null

  @Basic
  @Column(name = "filter")
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
  @JoinColumn(name = "userId", referencedColumnName = "id")
  open var user: UserEntity? = null

  @Basic
  @Column(name = "userId", nullable = false, insertable = false, updatable = false)
  open var userId: UUID? = null

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

