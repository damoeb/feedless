package org.migor.rich.rss.database2.models

import org.migor.rich.rss.database2.EntityWithUUID
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table
open class FeedEntity: EntityWithUUID() {

    @Basic
    @Column(name = "feed_url", nullable = false)
    open var feedUrl: String? = null

    @Basic
    @Column(name = "home_page_url")
    open var homePageUrl: String? = null

    @Basic
    @Column(name = "domain", nullable = false)
    open var domain: String? = null

    @Basic
    @Column(name = "title")
    open var title: String? = null

    @Basic
    @Column(name = "description")
    open var description: String? = null

    @Basic
    @Column(name = "status", nullable = false)
    open var status: String? = null

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
    open var harvestSite: Boolean? = null

    @Basic
    @Column(name = "allowHarvestFailure", nullable = false)
    open var allowHarvestFailure: Boolean? = null

    @Basic
    @Column(name = "lastUpdatedAt")
    open var lastUpdatedAt: java.sql.Timestamp? = null

    @Basic
    @Column(name = "managed_by_generic_feed", nullable = false)
    open var managedBy: Boolean? = null

    @Basic
    @Column(name = "lastStatusChangeAt")
    open var lastStatusChangeAt: java.sql.Timestamp? = null

    @Basic
    @Column(name = "failed_attempt_count", nullable = false)
    open var failedAttemptCount: Int? = null

}

