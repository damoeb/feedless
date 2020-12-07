package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.EntryDto
import org.migor.rss.rich.dto.FeedDto
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "t_feed")
class Feed {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(nullable = false)
  var title: String? = null

  @Column
  var description: String? = null

  @Column(nullable = false)
  var link: String? = null

  @Column
  var name: String? = null

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id")
  var subscription: Subscription? = null

  @Column(name = "subscription_id",
    updatable = false, insertable = false)
  var subscriptionId: String? = null

  @Basic
  var createdAt = Date()

  fun toDto(entries: List<EntryDto?>?): FeedDto? = FeedDto(id, title, link, name, description, createdAt, entries, subscriptionId)
}
