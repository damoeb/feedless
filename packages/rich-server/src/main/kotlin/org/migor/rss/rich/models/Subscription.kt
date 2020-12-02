package org.migor.rss.rich.models

import org.hibernate.annotations.GenericGenerator
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

  @ManyToOne(fetch = FetchType.EAGER)
  var harvestFrequency: HarvestFrequency? = null

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "owner_id", insertable = false, updatable = false )
  var owner: User? = null

//  @ReadOnlyProperty
//  @Column(name = "owner_id", length = 30,
//    updatable = false, insertable = false)
//  var ownerId: String? = null
}
