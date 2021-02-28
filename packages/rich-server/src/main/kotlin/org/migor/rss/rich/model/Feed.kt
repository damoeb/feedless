package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.FeedDto
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "t_feed")
class Feed() {
  constructor(name: String, owner: User, accessPolicy: AccessPolicy, priority: Int = 100) : this() {
    this.name = name
    this.owner = owner
    this.accessPolicy = accessPolicy
    this.priority = priority
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(nullable = false)
  var name: String? = null

  @Lob
  var description: String? = null

  @Basic
  var accessPolicy = AccessPolicy.PUBLIC

  @Basic
  var priority: Int? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  var owner: User? = null

  @Column(name = "owner_id",
    updatable = false, insertable = false)
  var ownerId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  var pubDate: Date? = null // todo mag this seems to be wrong

  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date? = null

//  todo mag add subscribers

  @OneToMany(targetEntity = FeedEntry::class, mappedBy = "feed", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
  var entries: List<FeedEntry> = ArrayList()

  fun toDto() = FeedDto(id, name, description, pubDate, ownerId, accessPolicy)

}
