package org.migor.rss.rich.models

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "rr_user")
class User {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var uuid: String? = null

  @Column(nullable = false)
  var apiKey: String? = null

  @OneToMany(targetEntity = Subscription::class, mappedBy = "owner", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
  val subscriptions: List<Subscription> = ArrayList()
}
