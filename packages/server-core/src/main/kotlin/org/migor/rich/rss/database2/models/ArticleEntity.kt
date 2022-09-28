package org.migor.rich.rss.database2.models

import org.migor.rich.rss.database.model.ArticleSource
import org.migor.rich.rss.database2.EntityWithUUID
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "t_article")
open class ArticleEntity : EntityWithUUID() {

  @Basic
  @Column(name = "updatedAt", nullable = false)
  open var updatedAt: Date? = null

  @Basic
  @Column(name = "date_published", nullable = false)
  open var publishedAt: Date? = null

  @Basic
  @Column(name = "is_released", nullable = false)
  open var released: Boolean = false

  @Basic
  @Column(name = "date_modified")
  open var modifiedAt: Date? = null

  @Basic
  @Column(name = "url")
  open var url: String? = null

  @Basic
  @Column(name = "title", nullable = false)
  open var title: String? = null

  @Basic
  @Column(name = "content_raw_mime")
  open var contentRawMime: String? = null

  @Basic
  @Column(name = "content_raw")
  open var contentRaw: String? = null

  @Basic
  @Column(name = "content_text", nullable = false)
  open var contentText: String? = null

  @Basic
  @Column(name = "has_content")
  open var hasContent: Boolean = false

//    @Basic
//    @Column(name = "enclosure")
//    open var enclosure: Any? = null

  @Basic
  @Column(name = "main_image_url")
  open var mainImageUrl: String? = null

  @Basic
  @Column(name = "score")
  open var score: Int = 0

  @NotNull
  @Column(name = "source_used")
  @Enumerated(EnumType.STRING)
  open var contentSource: ArticleSource = ArticleSource.FEED
}

