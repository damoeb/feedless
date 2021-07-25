package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.database.enums.FeedStatus
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "Feed")
class Feed {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(name = "title", nullable = false)
  var title: String? = null

  @Column(name = "feed_url", nullable = false)
  var feedUrl: String? = null

  @Column(name = "home_page_url")
  var homePageUrl: String? = null

  @Column(name = "fulltext_data", columnDefinition = "TEXT")
  var fulltext: String? = null

  @Column(name = "description", columnDefinition = "TEXT")
  var description: String? = null

  @Column(name = "tags", columnDefinition = "JSON")
  var tags: String? = null

  @Column(name = "lang")
  var lang: String? = null

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  var status: FeedStatus = FeedStatus.ok

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updatedAt")
  var updatedAt: Date? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "createdAt")
  var createdAt: Date? = null

  @Column(name = "streamId")
  var streamId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "nextHarvestAt")
  var nextHarvestAt: Date? = null

  @Column(name = "harvestIntervalMinutes")
  var harvestIntervalMinutes: Int? = null

}
