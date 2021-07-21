package org.migor.rss.rich.database.model

import org.springframework.validation.annotation.Validated
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Validated
@Table(name = "t_subscription_group")
class SubscriptionGroup() : Throttled() {
  constructor(name: String, owner: User) : this() {
    this.name = name
    this.owner = owner
  }

  @Column
  @NotNull
  var name: String? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  var owner: User? = null

  @Column(name = "owner_id",
    updatable = false, insertable = false)
  var ownerId: String? = null

}
