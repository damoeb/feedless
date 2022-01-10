package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.migor.rss.rich.database.enums.FeedStatus
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.validation.constraints.NotNull

@Entity
@Table(name = "\"Feed\"")
class Feed : JsonSupport() {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(name = "title", nullable = false)
  var title: String? = null

  @Column(name = "author", nullable = false)
  var author: String? = null

  @Column(name = "feed_url", nullable = false)
  var feedUrl: String? = null

  @Column(name = "feed_url_auth_header")
  var feedUrlAuthHeader: String? = null

  @Column(name = "domain", nullable = false)
  var domain: String? = null

  @Column(name = "home_page_url")
  var homePageUrl: String? = null

  @Column(name = "filter")
  var filter: String? = null

  @Column(name = "description", columnDefinition = "TEXT")
  var description: String? = null

  @Column(name = "retention_size")
  var retentionSize: Int? = null

  @Column(name = "tags", columnDefinition = "JSONB")
  @Type(type = "jsonb")
  @Basic(fetch = FetchType.LAZY)
  var tags: List<NamespacedTag>? = null

  @Column(name = "broken")
  var broken: Boolean = false

  @Column(name = "harvest_site")
  var harvestSite: Boolean = true

  @Column(name = "harvest_prerender")
  var harvestPrerender: Boolean = false

  @Column(name = "\"allowHarvestFailure\"")
  var allowHarvestFailure: Boolean = false

  @NotNull
  @Column(name = "failed_attempt_count")
  var failedAttemptCount: Int = 0

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  var status: FeedStatus = FeedStatus.ok

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"lastUpdatedAt\"")
  var lastUpdatedAt: Date? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"createdAt\"")
  var createdAt: Date? = null

  @Column(name = "\"streamId\"")
  var streamId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"nextHarvestAt\"")
  var nextHarvestAt: Date? = null

  @Column(name = "\"harvestIntervalMinutes\"")
  var harvestIntervalMinutes: Int? = null

  @NotNull
  @Column(name = "\"ownerId\"")
  lateinit var ownerId: String
}
