package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.JsonUtil
import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.harvest.FilterOperators
import org.springframework.validation.annotation.Validated
import java.util.*
import javax.persistence.*
import javax.validation.constraints.*


@Entity
@Validated
@Table(name = "t_subscription")
class Subscription {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Basic
  @NotNull
  @NotBlank
  var name: String? = null

  @Basic
  @NotNull
  @Pattern(regexp = "http[s]?://.{4,}")
  var url: String? = null

  @Transient
  var filter: List<Triple<String, FilterOperators, String>>? = null

  @Lob
  var filterJson: String? = null

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  var status: SubscriptionStatus = SubscriptionStatus.ACTIVE

  @ManyToOne(fetch = FetchType.EAGER)
  var harvestFrequency: HarvestFrequency? = null

  @Basic
  @Max(30)
  @Min(2)
  var feedSize: Number? = null

  @Basic
//  @FutureOrPresent
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
    filterJson = JsonUtil.gson.toJson(filter)
  }

  @PostLoad
  fun postLoad() {
    filter = JsonUtil.gson.fromJson<List<Triple<String, FilterOperators, String>>>(filterJson, List::class.java)
  }
}
