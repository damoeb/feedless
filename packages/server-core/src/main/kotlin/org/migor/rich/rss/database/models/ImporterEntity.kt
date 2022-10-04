package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import org.migor.rich.rss.database.enums.ImporterRefreshTrigger
import org.migor.rich.rss.database.enums.ImporterTargetType
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
@Table(name = "t_importer")
open class ImporterEntity : EntityWithUUID() {

  @Basic
  open val filter: String? = null

  @Basic
  @Column(name = "is_approve_manually", nullable = false)
  open var isApproveManually: Boolean = false

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "trigger_scheduled_next_at")
  open var triggerScheduledNextAt: Date? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "trigger_scheduled_last_at")
  open var triggerScheduledLastAt: Date? = null

  @Column(name = "trigger_refresh_on", nullable = false)
  @Enumerated(EnumType.STRING)
  open var triggerRefreshOn: ImporterRefreshTrigger = ImporterRefreshTrigger.CHANGE

  @Column(name = "segment_look_ahead_min")
  open var lookAheadMin: Int? = null

  @Column(name = "trigger_scheduled")
  open var triggerScheduleExpression: String? = null

  @Column(name = "segment_sort_field")
  open var segmentSortField: String? = null

  @Column(name = "segment_sort_asc", nullable = false)
  open var segmentSortAsc: Boolean = true

  @Column(name = "segment_digest", nullable = false)
  open var digest: Boolean = false

  @Column(name = "segment_size")
  open var segmentSize: Int? = null

  @Column(name = "bucketId", nullable = true, insertable = false, updatable = false)
  open var bucketId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bucketId", referencedColumnName = "id")
  open var bucket: BucketEntity? = null // todo mag rename to target

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feedId", referencedColumnName = "id")
  open var feed: NativeFeedEntity? = null // todo mag rename to source

  @Basic
  @Column(name = "feedId", insertable = false, updatable = false)
  open var feedId: UUID? = null

  @Basic
  @Column(name = "is_active")
  open var active: Boolean = true

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "lastUpdatedAt")
  open var lastUpdatedAt: Date? = null

  // https://www.baeldung.com/java-hibernate-map-postgresql-array
//  @Column(columnDefinition = "text[]")
//  @Type(type = "com.baeldung.hibernate.arraymapping.CustomStringArrayType")
  @Transient
  open var targets: Array<ImporterTargetType> = emptyArray()

//  @Basic
//  @Column(name = "ownerId", nullable = true, insertable = false, updatable = false)
//  open var ownerId: UUID? = null
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "ownerId", referencedColumnName = "id")
//  open var owner: UserEntity? = null
}
