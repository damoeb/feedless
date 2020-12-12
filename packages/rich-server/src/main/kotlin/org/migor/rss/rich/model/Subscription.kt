package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.SubscriptionDto
import org.springframework.validation.annotation.Validated
import java.util.*
import javax.persistence.*


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

//  @PrePersist
//  @PreUpdate
//  fun prePersist() {
//    lastErrorJson = JsonUtil.gson.toJson(lastException)
//  }
//
//  @PostLoad
//  fun postLoad() {
//    lastException = JsonUtil.gson.fromJson<HarvestException>(lastErrorJson, HarvestException::class.java)
//  }
}
