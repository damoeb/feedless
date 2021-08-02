package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.harvest.score.ArticleScores
import org.migor.rss.rich.service.Readability
import org.migor.rss.rich.util.JsonUtil
import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Table(name = "Article")
class Article {
  fun linkCount(): Int {
    return 0
  }

  fun toDto(): ArticleJsonDto {
    return ArticleJsonDto(
      id = this.id!!,
      title = this.title!!,
      url = this.url!!,
      author = this.author,
      tags = this.tags,
      enclosures = this.enclosures,
      commentsFeedUrl = this.commentsFeedUrl,
      content_text = this.content!!,
      content_html = this.contentHtml,
      date_published = this.pubDate
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

  @Column(name = "readability", columnDefinition = "JSON")
  var readabilityJson: String? = null

  @Transient
  var readability: Readability? = null

  @Column(name = "has_readability")
  var hasReadability: Boolean? = null

  @Column(name = "author")
  var author: String? = null

  @Column(name = "source_url")
  var sourceUrl: String? = null

  @Column(name = "applyPostProcessors")
  var applyPostProcessors: Boolean = true

  @Column(name = "released")
  var released: Boolean = true

  @Column(name = "tags", columnDefinition = "JSON")
  var tagsJson: String? = null

  @Transient
  var tags: Array<String>? = null

  @Column(name = "enclosure", columnDefinition = "JSON")
  var enclosures: String? = null

  @Column(name = "comment_feed_url")
  var commentsFeedUrl: String? = null

  @Column(name = "content_html", columnDefinition = "LONGTEXT")
  var contentHtml: String? = null

  @Column(name = "content_text", columnDefinition = "LONGTEXT")
  var content: String? = null

  @Column(name = "scores", columnDefinition = "JSON")
  var scoresJson: String? = null

  @Transient
  var scores: ArticleScores? = null

  @NotNull
  @Column(name = "score")
  var score: Float = 0f

  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  var lastScoredAt: Date = Date()

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "createdAt")
  var createdAt = Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "date_published")
  var pubDate = Date()

  @PrePersist
  @PreUpdate
  fun prePersist() {
    readability?.let {
      readabilityJson = JsonUtil.gson.toJson(readability)
    }
    tags?.let {
      tagsJson = JsonUtil.gson.toJson(tags)
    }
    scores?.let {
      scoresJson = JsonUtil.gson.toJson(scores)
    }
  }

  @PostLoad
  fun postLoad() {
    readabilityJson?.let {
      readability = JsonUtil.gson.fromJson(readabilityJson, Readability::class.java)
    }
    tagsJson?.let {
      tags = JsonUtil.gson.fromJson(tagsJson, Array<String>::class.java)
    }
    scoresJson?.let {
      scores = JsonUtil.gson.fromJson(scoresJson, ArticleScores::class.java)
    }
  }
}
