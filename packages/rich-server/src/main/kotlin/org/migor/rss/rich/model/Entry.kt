package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.FeedUtils
import org.migor.rss.rich.JsonUtil
import org.migor.rss.rich.dto.EntryDto
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "t_entry")
class Entry {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Transient
  var content: Map<String, Any>? = null

  @Lob
  var contentJson: String? = null

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

  fun toDto(): EntryDto? {
    val entryDto = EntryDto()
    content?.let { entryDto.putAll(it) }
    entryDto.put("id", FeedUtils.toURI(id!!, subscriptionId!!, createdAt))
    return entryDto
  }

  @PrePersist
  @PreUpdate
  fun prePersist() {
    contentJson = JsonUtil.gson.toJson(content)
  }

  @PostLoad
  fun postLoad() {
    content = JsonUtil.gson.fromJson<Map<String, Any>>(contentJson, Map::class.java)
  }
}
