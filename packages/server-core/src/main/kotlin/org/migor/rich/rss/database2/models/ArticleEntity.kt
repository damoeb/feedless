package org.migor.rich.rss.database2.models

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.database.model.ArticleSource
import org.migor.rich.rss.database2.EntityWithUUID
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Lob
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "t_article")
open class ArticleEntity : EntityWithUUID() {

  companion object {
    const val LEN_TITLE = 256
    const val LEN_URL = 1000
  }

  @Basic
  @Column(name = "url", nullable = false, length = LEN_URL)
  open var url: String? = null

  @Basic
  @Column(name = "title", nullable = false, length = LEN_TITLE)
  open var title: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, NativeFeedEntity.LEN_TITLE)
    }

  @Basic
  @Column(name = "content_raw_mime")
  open var contentRawMime: String? = null

  @Lob
  @Column(name = "content_raw")
  open var contentRaw: String? = null

  @Lob
  @Column(name = "content_text", nullable = false)
  open var contentText: String? = null

  @Basic
  @Column(name = "has_content")
  open var hasContent: Boolean = false

  @Basic
  @Column(name = "main_image_url", length = LEN_URL)
  open var mainImageUrl: String? = null

  @NotNull
  @Column(name = "source_used")
  @Enumerated(EnumType.STRING)
  open var contentSource: ArticleSource = ArticleSource.FEED

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
  @Column(name = "score")
  open var score: Int = 0
}

