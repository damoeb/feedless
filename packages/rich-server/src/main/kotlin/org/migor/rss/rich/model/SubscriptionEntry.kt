package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import javax.persistence.*


@Entity
@Table(name = "t_subscription_entry")
class SubscriptionEntry() {
  constructor(entry: SourceEntry, subscription: Subscription) : this() {
    this.entry = entry
    this.subscription = subscription
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
  @JoinColumn(name = "subscription_id")
  var subscription: Subscription? = null

  @Column(name = "subscription_id",
    updatable = false, insertable = false)
  var subscriptionId: String? = null

  @OneToOne(cascade = [CascadeType.DETACH], fetch = FetchType.EAGER, orphanRemoval = false)
  @JoinColumn(name = "entry_id")
  var entry: SourceEntry? = null

  @Column(name = "entry_id",
    updatable = false, insertable = false)
  var entryId: String? = null

}
