package org.migor.rich.rss.database.model

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
@Table(name = "\"ArticleExporter\"")
class Exporter {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "trigger_scheduled_next_at")
  var triggerScheduledNextAt: Date? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "trigger_scheduled_last_at")
  var triggerScheduledLastAt: Date? = null

  @Column(name = "trigger_refresh_on")
  var triggerRefreshOn: String = "change"

  @Column(name = "segment_look_ahead_min")
  var lookAheadMin: Int? = null

  @Column(name = "trigger_scheduled")
  var triggerScheduleExpression: String? = null

  @Column(name = "segment_sort_field")
  var segmentSortField: String? = null

  @Column(name = "segment_sort_asc")
  var segmentSortAsc: Boolean = true

  @Column(name = "segment_digest")
  var digest: Boolean = false

  @Column(name = "segment_size")
  var segmentSize: Int? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"lastUpdatedAt\"")
  var lastUpdatedAt: Date? = null

  @Column(name = "\"bucketId\"")
  var bucketId: String? = null
}
