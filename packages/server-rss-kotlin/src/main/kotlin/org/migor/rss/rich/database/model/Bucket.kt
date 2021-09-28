package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Table(name = "Bucket")
class Bucket {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "streamId")
  var streamId: String? = null

  @NotNull
  @Column(name = "title")
  var title: String? = null

  @NotNull
  @Column(name = "ownerId")
  var ownerId: String? = null

  @Column(name = "segment_allocation_by_content", columnDefinition = "TEXT")
  var filterExpression: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "lastPostProcessedAt")
  var lastPostProcessedAt: Date? = null

//  @Column(name = "retention_policy", columnDefinition = "JSON")
//  var retentionPolicy: String? = null

//  @Column(name = "content_resolution", columnDefinition = "JSON")
//  var contentResolution: String? = null

//  @Column(name = "replay_policy", columnDefinition = "JSON")
//  var replayPolicy: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "lastUpdatedAt")
  var lastUpdatedAt: Date? = null

  @Column(name = "trigger_refresh_on")
  var triggerRefreshOn: String = "change"

}
