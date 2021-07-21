package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import java.time.temporal.ChronoUnit
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull


@MappedSuperclass
abstract class Throttled {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column
  @NotNull
  var throttled: Boolean = false

  @Temporal(TemporalType.TIMESTAMP)
  var nextEntryReleaseAt: Date? = null

  @Basic
  @Min(2)
  @Max(40)
  var releaseBatchSize: Int? = 10

  @Basic
  @Min(1)
  @Max(365)
  var releaseInterval: Long? = null

  @Basic
  var releaseTimeUnit: ChronoUnit? = null

  @PrePersist
  @PreUpdate
  fun prePersistThrottled() {
    if (throttled) {
      if (releaseInterval == null) {
        throw IllegalArgumentException("When subscription is throttled, releaseInterval has to be set")
      }
      if (releaseTimeUnit == null) {
        throw IllegalArgumentException("When subscription is throttled, releaseTimeUnit has to be set")
      }
      if (releaseBatchSize == null) {
        throw IllegalArgumentException("When subscription is throttled, releaseBatchSize has to be set")
      }
    } else {
      nextEntryReleaseAt = null
    }
  }
}
