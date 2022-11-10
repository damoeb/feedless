package org.migor.rich.rss.database.models

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.database.EntityWithUUID
import org.migor.rich.rss.database.enums.NativeFeedStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "t_feed_native")
open class NativeFeedEntity : EntityWithUUID() {

  @Transient
  private val log = LoggerFactory.getLogger(NativeFeedEntity::class.simpleName)

  companion object {
    const val LEN_TITLE = 256
    const val LEN_URL = 1000
  }

  @Basic
  @Column(name = "domain")
  open var domain: String? = null

  @Basic
  @Column(name = "website_url")
  open var websiteUrl: String? = null

  @Basic
  @Column(name = "feed_url", nullable = false, length = LEN_URL, unique = true)
  open var feedUrl: String? = null

  @Basic
  @Column(name = "title", nullable = false, length = LEN_TITLE)
  open var title: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, LEN_TITLE)
      logLengthViolation("title", value, field)
    }

  private fun logLengthViolation(field: String, expectedValue: String?, actualValue: String?) {
    actualValue?.let {
      if (expectedValue!!.length != actualValue.length) {
        log.warn("Persisted value for '${field}' transformed from '$expectedValue' -> '${actualValue}'")
      }
    }
  }

  @Basic
  @Column(name = "description", length = 1024)
  open var description: String? = null

  @Basic
  @Column(name = "harvestIntervalMinutes")
  open var harvestIntervalMinutes: Int? = null

  @Basic
  @Column(name = "nextHarvestAt")
  open var nextHarvestAt: Date? = null

  @Basic
  @Column(name = "retention_size")
  open var retentionSize: Int? = null

  @Basic
  @Column(name = "harvest_site", nullable = false)
  open var harvestSite: Boolean = false

  @Basic
  @Column(name = "harvest_site_with_prerender", nullable = false)
  open var harvestSiteWithPrerender: Boolean = false

  @Basic
  @Column(name = "lastUpdatedAt")
  open var lastUpdatedAt: Date? = null

//  @Basic
//  @Column(name = "managed_by", nullable = false)
//  @Enumerated(EnumType.STRING)
//  open var managedBy: FeedManagerType = FeedManagerType.USER

//  @Basic
//  @Column(name = "lastStatusChangeAt")
//  open var lastStatusChangeAt: Date? = null

  @Basic
  @Column(name = "failed_attempt_count", nullable = false)
  open var failedAttemptCount: Int = 0

  @Basic
  open var lat: Long? = null

  @Basic
  open var lon: Long? = null

  @Basic
  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  open var status: NativeFeedStatus = NativeFeedStatus.OK

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "managingFeed")
  open var managedBy: GenericFeedEntity? = null

  @Basic
  @Column(name = "streamId", nullable = false, insertable = false, updatable = false)
  open var streamId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "streamId", referencedColumnName = "id")
  open var stream: StreamEntity? = null

}
