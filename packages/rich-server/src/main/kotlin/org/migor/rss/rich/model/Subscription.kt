package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.JsonUtil
import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.dto.ThrottleDto
import org.migor.rss.rich.filter.EntryFilter
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

  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date? = null

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  var owner: User? = null

  @Column(name = "owner_id",
    updatable = false, insertable = false)
  var ownerId: String? = null

  @OneToOne(cascade = [CascadeType.DETACH], fetch = FetchType.EAGER, orphanRemoval = false)
  @JoinColumn(name = "source_id")
  var source: Source? = null

  @Column(name = "source_id",
    updatable = false, insertable = false)
  var sourceId: String? = null

//  @OneToMany(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY, orphanRemoval = true)
//  var entries: List<SubscriptionEntry>? = null

  @ManyToOne(cascade = [CascadeType.DETACH], fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  var group: SubscriptionGroup? = null

  @Column(name = "group_id",
    updatable = false, insertable = false)
  var groupId: String? = null

  @Column
  @NotNull
  var filtered: Boolean = false

  @Column
  @NotNull
  var managed: Boolean = false

  @Transient
  var filters: List<EntryFilter>? = null

  @Lob
  var filtersJson: String? = null

  @PostLoad
  fun postLoad() {
    filters = JsonUtil.gson.fromJson<List<EntryFilter>>(filtersJson, List::class.java)
  }

  @PrePersist
  @PreUpdate
  fun prePersist() {
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
    filtered = filters !=null && !filters!!.isEmpty()
    filtersJson = JsonUtil.gson.toJson(filters)

    managed = throttled || filtered
  }

  fun toDto(): SubscriptionDto {
    val throttle = ThrottleDto(releaseBatchSize, releaseInterval, releaseTimeUnit.toString().toLowerCase())
    return SubscriptionDto(id, source!!.title, source!!.description, source!!.updatedAt, source!!.url, throttled, source!!.id, groupId, ownerId, throttle)
  }
}
