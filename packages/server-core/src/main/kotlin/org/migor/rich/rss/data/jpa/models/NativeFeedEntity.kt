package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.data.jpa.EntityWithUUID
import org.migor.rich.rss.data.jpa.enums.BucketVisibility
import org.migor.rich.rss.data.jpa.enums.NativeFeedStatus
import org.slf4j.LoggerFactory
import java.util.*

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
  open var domain: String? = null

  @Basic
  open var websiteUrl: String? = null

  @Basic
  open var imageUrl: String? = null

  @Basic
  open var iconUrl: String? = null

  @Basic
  open var lang: String? = null

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open var visibility: BucketVisibility = BucketVisibility.public

  @Basic
  @Column(name = "ownerId", nullable = false, insertable = false, updatable = false)
  open var ownerId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ownerId", referencedColumnName = "id")
  open var owner: UserEntity? = null


  @Basic
  @Column(nullable = false, length = LEN_URL, unique = true)
  open lateinit var feedUrl: String

  @Basic
  @Column(nullable = false, length = LEN_TITLE, unique = true)
  open var title: String = ""
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
  @Column(length = 1024)
  open var description: String? = null

  @Basic
  open var harvestIntervalMinutes: Int? = null

  @Basic
  open var nextHarvestAt: Date? = null

  @Basic
  open var retentionSize: Int? = null

  @Basic
  @Column(nullable = false)
  open var harvestItems: Boolean = false

  @Basic
  @Column(nullable = false)
  open var inlineImages: Boolean = false

  @Basic
  @Column(nullable = false)
  open var harvestSiteWithPrerender: Boolean = false

  @Basic
  open var lastUpdatedAt: Date? = null

//  @Basic
//  @Column(name = "managed_by", nullable = false)
//  @Enumerated(EnumType.STRING)
//  open var managedBy: FeedManagerType = FeedManagerType.USER

//  @Basic
//  @Column(name = "lastStatusChangeAt")
//  open var lastStatusChangeAt: Date? = null

  @Basic
  @Column(nullable = false)
  open var failedAttemptCount: Int = 0

  @Basic
  open var lat: Long? = null

  @Basic
  open var lon: Long? = null

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open var status: NativeFeedStatus = NativeFeedStatus.OK

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "managingFeed")
  open var managedBy: GenericFeedEntity? = null

  @Basic
  @Column(name = "streamId", nullable = false, insertable = false, updatable = false)
  open var streamId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
  @JoinColumn(name = "streamId", referencedColumnName = "id")
  open var stream: StreamEntity? = null

}
