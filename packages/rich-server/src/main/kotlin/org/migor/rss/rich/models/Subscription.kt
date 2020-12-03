package org.migor.rss.rich.models

import org.hibernate.annotations.GenericGenerator
import org.jetbrains.annotations.NotNull
import org.migor.rss.rich.dtos.SubscriptionDto
import org.springframework.data.annotation.ReadOnlyProperty
import org.springframework.validation.annotation.Validated
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id" )
  var owner: User? = null

  @Column(name = "owner_id",
    updatable = false, insertable = false)
  var ownerId: String? = null

  fun toDto() = SubscriptionDto(uuid, name, status, ownerId, harvestFrequency?.toDto())
}
