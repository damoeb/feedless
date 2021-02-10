package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.SubscriptionDto
import org.springframework.validation.annotation.Validated
import java.time.temporal.ChronoUnit
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull


@Entity
@Validated
@Table(name = "t_subscription")
class Subscription() {

  constructor(user: User, source: Source) : this() {
    this.owner = user
    this.source = source
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column
  @NotNull
  var forwardToPublic: Boolean = true

  @Column
  @NotNull
  var publicSource: Boolean = true

  @Column
  @NotNull
  var throttled: Boolean = false

  @Temporal(TemporalType.TIMESTAMP)
  var nextEntryReleaseAt: Date = Date()

  @Basic
  @Min(2)
  @Max(40)
  var releaseBatchSize: Int? = 10

  @Basic
  @Min(4)
  @Max(168)
  var releaseInterval: Long? = null

  @Basic
  var releaseTimeUnit: ChronoUnit? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  var owner: User? = null

  @Column(name = "owner_id",
    updatable = false, insertable = false)
  var ownerId: String? = null

  @OneToOne(cascade = [CascadeType.DETACH], fetch = FetchType.EAGER, orphanRemoval = false)
  var source: Source? = null

  @PrePersist
  @PreUpdate
  fun prePersist() {
    if (throttled) {
      if (releaseInterval == null) {
        throw IllegalArgumentException("When subscription is trottled, releaseInterval has to be set")
      }
      if (releaseTimeUnit == null) {
        throw IllegalArgumentException("When subscription is trottled, releaseTimeUnit has to be set")
      }
      if (releaseBatchSize == null) {
        throw IllegalArgumentException("When subscription is trottled, releaseBatchSize has to be set")
      }
      nextEntryReleaseAt = Date()
    }
  }

  fun toDto(): SubscriptionDto {
    return SubscriptionDto(id, source!!.title, source!!.description, source!!.lastUpdatedAt, source!!.id)
  }
}
