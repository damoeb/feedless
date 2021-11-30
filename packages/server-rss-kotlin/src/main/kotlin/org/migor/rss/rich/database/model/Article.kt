package org.migor.rss.rich.database.model

import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.service.ArticleService
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Transient
import javax.validation.constraints.NotNull

@Entity
@Table(name = "\"Article\"")
class Article: JsonSupport() {
  @Transient
  private val log = LoggerFactory.getLogger(Article::class.simpleName)

  fun linkCount(): Int {
    val linkCount = ArticleService.getLinkCount(this)
    log.info("article $url has linkCount $linkCount")
    return linkCount
  }

  fun toDto(date_published: Date? = null): ArticleJsonDto {
    val mime = "text/html"
    return ArticleJsonDto(
      id = this.id!!,
      title = this.title!!,
      url = this.url!!,
      author = this.author,
      tags = this.tags?.map { tag -> "${tag.namespace}:${tag.tag}" },
      enclosures = null,
      commentsFeedUrl = this.commentsFeedUrl,
      content_text = this.contentText,
      content_raw = this.getContentOfMime(mime),
      content_raw_mime = mime,
      date_published = Optional.ofNullable(date_published).orElse(this.pubDate)
    )
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "title")
  var title: String? = null

  @Column(name = "url", columnDefinition = "TEXT")
  var url: String? = null

  @Column(name = "has_readability")
  var hasReadability: Boolean = false

  @Column(name = "has_harvest")
  var hasHarvest: Boolean = false

  @Column(name = "word_count_text")
  var wordsCountText: Int? = null

  @Column(name = "author")
  var author: String? = null

  @Column(name = "source_url")
  var sourceUrl: String? = null

  @Column(name = "released")
  var released: Boolean = true

  @Column(name = "tags", columnDefinition = "JSONB")
  @Type(type = "jsonb")
  @Basic(fetch = FetchType.LAZY)
  var tags: List<NamespacedTag>? = null

  @Column(name = "data_json_map", columnDefinition = "JSONB")
  @Type(type = "jsonb")
  @Basic(fetch = FetchType.LAZY)
  var data: HashMap<String, Any> = HashMap()

//  @Column(name = "enclosure", columnDefinition = "JSONB")
//  var enclosures: String? = null

  @Column(name = "comment_feed_url")
  var commentsFeedUrl: String? = null

  @Column(name = "content_text")
  var contentText: String = ""

  @Column(name = "content_raw")
  var contentRaw: String? = null

  @Column(name = "content_raw_mime")
  var contentRawMime: String? = null

  @NotNull
  @Column(name = "score")
  var score: Double = 0.0

  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"lastScoredAt\"")
  var lastScoredAt: Date = Date()

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"createdAt\"")
  var createdAt = Date()

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"updatedAt\"")
  var updatedAt = Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "date_published")
  var pubDate = Date()

  fun putDynamicField(namespace: String, key: String, data: Any) {
    this.data.put("${namespace}__$key", data)
  }

  @PrePersist
  @PreUpdate
  fun prePersist() {
    if (title != null && title!!.length > 200) {
      title = title?.substring(0, 197) + "..."
    }
  }


  fun getContentOfMime(mime: String): String? {
    return if (mime == this.contentRawMime) {
      StringUtils.trimToNull(this.contentRaw)
    } else {
      null
    }
  }
}
