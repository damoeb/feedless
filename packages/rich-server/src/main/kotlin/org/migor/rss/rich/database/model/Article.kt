package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.api.dto.ArticleJsonDto
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

  @Column(name = "author")
  var author: String? = null

  @Column(name = "source_url")
  var sourceUrl: String? = null

  @Column(name = "tags", columnDefinition = "JSON")
  var tags: String? = null

  @Column(name = "enclosure", columnDefinition = "JSON")
  var enclosures: String? = null

  @Column(name = "comment_feed_url")
  var commentsFeedUrl: String? = null

  @Column(name = "content_html", columnDefinition = "LONGTEXT")
  var contentHtml: String? = null

  @Column(name = "content_text", columnDefinition = "LONGTEXT")
  var content: String? = null

  @NotNull
  @Column(name = "score")
  var score: Double = 0.0

  // -- fulltext ---------------------------------------------------------------------------------------------------- --

//  @Basic
//  @NotNull
//  var hasFulltext = false
//
//  @Lob
//  var fulltextHtml: String? = null

  // ---------------------------------------------------------------------------------------------------------------- --

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "createdAt")
  var createdAt = Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "date_published")
  var pubDate = Date()
}
