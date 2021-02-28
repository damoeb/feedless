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

  constructor(user: User, source: Source) : this() {
    this.owner = user
    this.source = source
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

  @PrePersist
  @PreUpdate
  fun prePersist() {
    prePersistThrottled()

    filtered = !StringUtils.isBlank(takeIf)

    managed = throttled || filtered

    if (RoutingPolicy.FORWARD.equals(routing) && StringUtils.isBlank(routingTarget)) {
      throw IllegalArgumentException("When subscription is using RoutingPolicy.FORWARD, a routingTarget has to be defined")
    }
  }

  fun toDto(): SubscriptionDto {
    val throttle = ThrottleDto(releaseBatchSize, releaseInterval, releaseTimeUnit.toString().toLowerCase())
    val title = if (title == null) source!!.title else title
    val description = if (description == null) source!!.description else description
    return SubscriptionDto(id, title, description, source!!.updatedAt, source!!.url, throttled, source!!.id, groupId, ownerId, throttle)
  }
}
