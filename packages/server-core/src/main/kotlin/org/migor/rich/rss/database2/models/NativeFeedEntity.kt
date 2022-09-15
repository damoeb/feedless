package org.migor.rich.rss.database2.models

import org.migor.rich.rss.database2.EntityWithUUID
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

@Entity
@Table(name = "t_feed_native")
open class NativeFeedEntity : EntityWithUUID() {

  @Basic
  @Column(name = "domain", nullable = false)
  open var domain: String? = null

  @Basic
  @Column(name = "website_url", nullable = false)
  open var websiteUrl: String? = null

  @Basic
  @Column(name = "feed_url", nullable = false, length = 1000)
  open var feedUrl: String? = null

  @Basic
  @Column(name = "title", nullable = false, length = 100)
  open var title: String? = null

  @Basic
  @Column(name = "description", length = 1024)
  open var description: String? = null

  @Basic
  @Column(name = "harvestIntervalMinutes")
  open var harvestIntervalMinutes: Int? = null

  @Basic
  @Column(name = "nextHarvestAt")
  open var nextHarvestAt: java.sql.Timestamp? = null

  @Basic
  @Column(name = "retention_size")
  open var retentionSize: Int? = null

  @Basic
  @Column(name = "harvest_site", nullable = false)
  open var harvestSite: Boolean = true

  @Basic
  @Column(name = "lastUpdatedAt")
  open var lastUpdatedAt: java.sql.Timestamp? = null

  @Basic
  @Column(name = "managed_by", nullable = false)
  @Enumerated(EnumType.STRING)
  open var managedBy: FeedManagerType = FeedManagerType.USER

  @Basic
  @Column(name = "lastStatusChangeAt")
  open var lastStatusChangeAt: java.sql.Timestamp? = null

  @Basic
  @Column(name = "failed_attempt_count", nullable = false)
  open var failedAttemptCount: Int = 0

  @Basic
  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  open var status: NativeFeedStatus = NativeFeedStatus.OK

}

enum class FeedManagerType {
  USER, GENERIC_FEED
}

enum class NativeFeedStatus {
  OK,
  DEACTIVATED
}
