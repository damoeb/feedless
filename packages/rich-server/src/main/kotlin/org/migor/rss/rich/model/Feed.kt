package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.FeedDto
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "feed")
class Feed {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var uuid: String? = null

  @Column(nullable = false)
  var title: String? = null

  @Column(nullable = false)
  var description: String? = null

  @Column(nullable = false)
  var link: String? = null

  @Column(nullable = false)
  var name: String? = null

  @OneToOne(mappedBy = "feed", fetch = FetchType.LAZY)
  val subscription: Subscription? = null

  @Basic
  var createdAt = Date()

  fun toDto(): FeedDto? = FeedDto(uuid, title, link, name, description, createdAt)
}
