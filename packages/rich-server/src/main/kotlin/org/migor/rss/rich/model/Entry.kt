package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.EntryDto
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "entry")
class Entry {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var uuid: String? = null

  @Lob
  var content: String? = null

  @Column(nullable = false)
  var link: String? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id")
  var subscription: Subscription? = null

  @Column(name = "subscription_id",
    updatable = false, insertable = false)
  var subscriptionId: String? = null

  @Basic
  var createdAt = Date()

  fun toDto(): EntryDto? = EntryDto(uuid, link, createdAt)
}
