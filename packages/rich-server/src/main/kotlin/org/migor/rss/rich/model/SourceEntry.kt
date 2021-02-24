package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.FeedUtil
import org.migor.rss.rich.JsonUtil
import org.migor.rss.rich.dto.SourceEntryDto
import java.net.URL
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Table(name = "t_source_entry")
class SourceEntry {
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

//  @Column
//  @NotNull
//  var title: String? = null
//
//  @Lob
//  var contentHtml: String? = null
//
//  @Lob
//  var content: String? = null

  @Column
  @NotNull
  var status: EntryStatus = EntryStatus.RAW

  @Column
  @NotNull
  var score: Double = 0.0

  // -- fulltext ---------------------------------------------------------------------------------------------------- --

  @Basic
  @NotNull
  var hasFulltext = false

  @Lob
  var fulltextHtml: String? = null

  // ---------------------------------------------------------------------------------------------------------------- --

  @Basic
  var lang: String? = null

  @Basic
  var langScore: Float? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_id")
  var source: Source? = null

  @Column(name = "source_id",
    updatable = false, insertable = false)
  var sourceId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  var createdAt = Date()

  @Temporal(TemporalType.TIMESTAMP)
  var pubDate = Date()

  fun toDto(): SourceEntryDto? {
    val entryDto = SourceEntryDto()
    content?.let { entryDto.putAll(it) }
    entryDto.put("id", FeedUtil.toURI(id!!, sourceId!!, createdAt))
    entryDto.put("score", score)
    entryDto.put("sourceId", sourceId!!)
    entryDto.put("pubDate", pubDate)
    entryDto.put("lang", lang!!)
    entryDto.put("langScore", langScore!!)
    entryDto.put("domain", URL(link).host.toLowerCase().replace("www.", ""))

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
