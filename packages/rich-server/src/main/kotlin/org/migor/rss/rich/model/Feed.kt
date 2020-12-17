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

  @Basic
  var copyright: String? = null

  @Basic
  var language: String? = null

  @Basic
  var pubDate: Date? = null

  @Basic
  var name: String? = null

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id")
  var subscription: Subscription? = null

  @Column(name = "subscription_id",
    updatable = false, insertable = false)
  var subscriptionId: String? = null

  @Basic
  var createdAt = Date()

  fun toDto(entries: List<EntryDto?>?): FeedDto? = FeedDto(id, title, link, name, description, createdAt, pubDate, language, copyright, entries, subscriptionId)
}
