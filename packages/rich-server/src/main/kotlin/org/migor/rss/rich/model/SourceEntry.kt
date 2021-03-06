package org.migor.rss.rich.model

import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.GenericGenerator
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.migor.rss.rich.FeedUtil
import org.migor.rss.rich.JsonUtil
import org.migor.rss.rich.dto.SourceEntryDto
import java.net.URL
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Table(name = "t_source_entry", uniqueConstraints = [
  UniqueConstraint(name = "unique_src_link", columnNames = ["link", "source_id"])
])
class SourceEntry {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Transient
  var properties: Map<String, Any>? = null

  @Lob
  var propertiesJson: String? = null

  @Column(nullable = false, length = 512)
  var link: String? = null

  @Lob
  @NotNull
  var title: String? = null

  @Lob
  var contentHtml: String? = null

  @Lob
  var content: String? = null

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
    properties?.let { entryDto.putAll(it) }
    entryDto.put("id", FeedUtil.toURI(id!!, sourceId!!, createdAt))
    entryDto.put("title", title!!)
    entryDto.put("content", content)
    entryDto.put("score", score)
    entryDto.put("sourceId", sourceId!!)
    entryDto.put("pubDate", pubDate)
    entryDto.put("domain", URL(link).host.toLowerCase().replace("www.", ""))

    return entryDto
  }

  @PrePersist
  @PreUpdate
  fun prePersist() {
    propertiesJson = JsonUtil.gson.toJson(properties)
  }

  @PostLoad
  fun postLoad() {
    properties = JsonUtil.gson.fromJson<Map<String, Any>>(propertiesJson, Map::class.java)
  }

  fun linkCount(): Int {
    return links().size
  }

  fun links(): List<String> {
    if (StringUtils.isBlank(contentHtml)) {
      return emptyList()
    }
    val doc = Jsoup.parse(contentHtml)
    return doc.select("a").map { element: Element -> element.attr("abs:href") }.filterNotNull()
  }
}
