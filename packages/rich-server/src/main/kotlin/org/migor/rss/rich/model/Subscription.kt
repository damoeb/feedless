package org.migor.rss.rich.model

import org.apache.commons.lang3.StringUtils
import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.dto.ThrottleDto
import org.springframework.validation.annotation.Validated
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Validated
@Table(name = "t_subscription")
class Subscription() : Throttled() {

  constructor(user: User, source: Source, tags: List<String>? = null) : this() {
    this.owner = user
    this.source = source
    this.tags = tags
  }

  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date? = null

  @Basic
  var title: String? = null

  @Basic
  var description: String? = null

  @Basic
  var contentLevel: ContentLevelPolicy = ContentLevelPolicy.FIRST_DEGREE_CONTENT

  @Basic
  var routing: RoutingPolicy? = null

  @Basic
  var routingTarget: String? = null

  @Basic
  var tagsData: String? = null

  @Basic
  @NotNull
  var throughput: String = "-"

  @Transient
  var tags: List<String>? = null

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

  @Basic
  var takeIf: String? = null


  @PostLoad
  fun postLoad() {
    tags = StringUtils.trimToEmpty(tagsData).split(",")
  }

  @PrePersist
  @PreUpdate
  fun prePersist() {
    prePersistThrottled()

    filtered = !StringUtils.isBlank(takeIf)

    managed = throttled || filtered

    tagsData = Optional.ofNullable(tags).orElse(emptyList()).joinToString(separator = ",")

    if (RoutingPolicy.FORWARD.equals(routing) && StringUtils.isBlank(routingTarget)) {
      throw IllegalArgumentException("When subscription is using RoutingPolicy.FORWARD, a routingTarget has to be defined")
    }
  }

  fun toDto(): SubscriptionDto {
    val throttle = ThrottleDto(releaseBatchSize, releaseInterval, releaseTimeUnit.toString().toLowerCase())
    val title = Optional.ofNullable(StringUtils.trimToNull(title)).orElse(Optional.ofNullable(StringUtils.trimToNull(source!!.title)).orElse(source!!.url))
    val description = if (description == null) source!!.description else description
    return SubscriptionDto(id = id,
      title = title,
      description = description,
      lastUpdatedAt = source!!.updatedAt,
      url = source!!.url,
      throttled = throttled,
      throttle = throttle,
      sourceId = source!!.id,
      groupId = groupId,
      ownerId = ownerId,
      throughput = throughput)
  }
}
