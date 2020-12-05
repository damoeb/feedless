package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.SubscriptionDto
import org.springframework.validation.annotation.Validated
import java.util.*
import javax.persistence.*


@Entity
@Validated
@Table(name = "subscription")
class Subscription {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var uuid: String? = null

  @Column(nullable = false)
  var name: String? = null

  @Column(nullable = false)
  var url: String? = null

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  var status: SubscriptionStatus = SubscriptionStatus.RUNNING

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
  @PrimaryKeyJoinColumn
  var feed: Feed? = null

  @Basic
  var isFeedUrl: Boolean? = null

  fun toDto() = SubscriptionDto(uuid, name, status, ownerId, harvestFrequency?.toDto())
}
