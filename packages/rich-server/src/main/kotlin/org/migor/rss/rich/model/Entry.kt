package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.FeedUtils
import org.migor.rss.rich.JsonUtil
import org.migor.rss.rich.dto.EntryDto
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


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

  @Column
  @NotNull
  var status: EntryStatus = EntryStatus.RAW

  @Column
  @NotNull
  var score: Double = 0.0

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_id")
  var source: Source? = null

  @Column(name = "source_id",
    updatable = false, insertable = false)
  var sourceId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  var createdAt = Date()

  fun toDto(): EntryDto? {
    val entryDto = EntryDto()
    content?.let { entryDto.putAll(it) }
    entryDto.put("id", FeedUtils.toURI(id!!, sourceId!!, createdAt))
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
