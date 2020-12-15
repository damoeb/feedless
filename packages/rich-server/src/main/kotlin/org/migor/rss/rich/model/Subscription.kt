package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.SubscriptionDto
import org.springframework.validation.annotation.Validated
import java.lang.IllegalArgumentException
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull


@Entity
@Validated
@Table(name = "t_subscription")
class Subscription {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(nullable = false)
  var name: String? = null

  @Column(nullable = false)
  var url: String? = null

  @Column
  @NotNull
  var throttled: Boolean = false

  @Basic
  var nextEntryReleaseAt: Date? = null

  @Basic
  @Min(4)
  @Max(168)
  var entryReleaseIntervalHours: Long? = null

  @Column
  @NotNull
  var withFulltext: Boolean = false

  @Column
  var rssProxyUrl: String? = null

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  var sourceType: SourceType? = null

//  @Transient
//  var feedOptions: Map<String, Any>? = null
//
//  @Column
//  var feedOptionsJson: String? = null

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  var status: SubscriptionStatus = SubscriptionStatus.ACTIVE

  @ManyToOne(fetch = FetchType.EAGER)
  var harvestFrequency: HarvestFrequency? = null

  @Basic
  var nextHarvestAt: Date? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id" )
  var owner: User? = null

  @Column(name = "owner_id",
    updatable = false, insertable = false)
  var ownerId: String? = null

  @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
  var feed: Feed? = null

  fun toDto() = SubscriptionDto(id, name, status, ownerId, harvestFrequency?.toDto())

  @PrePersist
  @PreUpdate
  fun prePersist() {
    if (throttled && entryReleaseIntervalHours == null) {
     throw IllegalArgumentException("When subscription is trottled, entryReleaseIntervalHours has to be set")
    }
//    feedOptionsJson = JsonUtil.gson.toJson(feedOptions)
  }
//
//  @PostLoad
//  fun postLoad() {
//    feedOptions = JsonUtil.gson.fromJson<Map<String, Any>>(feedOptionsJson, Map::class.java)
//  }
}
