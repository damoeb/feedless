package org.migor.feedless.jpa.report

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.jpa.repository.PluginExecution
import org.migor.feedless.jpa.repository.RepositoryEntity
import org.springframework.context.annotation.Lazy
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Entity
@Table(name = "t_segment")
open class SegmentationEntity : EntityWithUUID() {

  @Column(nullable = false, name = "report_max_size")
  open var size: Int = 0

  @Column(nullable = false, name = "time_segment__starting_at")
  open lateinit var timeSegmentStartingAt: LocalDateTime

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "time_segment__increment")
  open lateinit var timeInterval: ChronoUnit

  @Column(name = "filter_latlon", columnDefinition = "geometry")
  open var contentSegmentLatLon: Point? = null

  @Column(name = "filter_latlon_distance")
  open var contentSegmentLatLonDistance: Double? = null

//  @JdbcTypeCode(Types.ARRAY)
//  @Column(name = "filter_tags", columnDefinition = "text[]")
//  open lateinit var contentSegmentTags: Array<String>

  @JdbcTypeCode(SqlTypes.JSON)
  @Lazy
  @Column(nullable = false, name = "report_plugin")
  open lateinit var reportPlugin: PluginExecution

  @Column(name = StandardJpaFields.repositoryId, nullable = false)
  open lateinit var repositoryId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.repositoryId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_segmentation__to__repository")
  )
  open var repository: RepositoryEntity? = null
}

//private fun ChronoUnit.toDto(): IntervalUnit {
//  return when (this) {
//    ChronoUnit.WEEKS -> IntervalUnit.WEEK
//    ChronoUnit.MONTHS -> IntervalUnit.MONTH
//    else -> throw IllegalArgumentException("Unsupported chronoUnit: $this")
//  }
//}
