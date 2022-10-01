package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
@Table(name = "t_subscription")
open class SubscriptionEntity : EntityWithUUID() {

  @Basic
  @Column(name = "bucketId", nullable = false, insertable = false, updatable = false)
  open var bucketId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bucketId", referencedColumnName = "id")
  open var bucket: BucketEntity? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feedId", referencedColumnName = "id")
  open var feed: NativeFeedEntity? = null

  @Basic
  @Column(name = "feedId", nullable = false, insertable = false, updatable = false)
  open var feedId: UUID? = null

  @Basic
  @Column(name = "is_active")
  open var active: Boolean = true

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "lastUpdatedAt")
  open var lastUpdatedAt: Date? = null
}

