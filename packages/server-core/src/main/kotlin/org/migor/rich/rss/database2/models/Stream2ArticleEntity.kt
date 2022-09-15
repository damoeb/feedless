package org.migor.rich.rss.database2.models

import org.migor.rich.rss.database2.EntityWithUUID
import java.sql.Timestamp
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "map_stream_to_article")
open class Stream2ArticleEntity : EntityWithUUID() {

  @Basic
  @Column(name = "updatedAt", nullable = false)
  open var updatedAt: Timestamp? = null

  @Basic
  @Column(name = "date_released", nullable = false)
  open var dateReleased: Timestamp? = null

  @Basic
  @Column(name = "is_released")
  open var released: Boolean = false

  @Basic
  @Column(name = "articleId", nullable = false, insertable = false, updatable = false)
  open lateinit var articleId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "articleId", referencedColumnName = "id")
  open var article: ArticleEntity? = null

  @Basic
  @Column(name = "streamId", nullable = false, insertable = false, updatable = false)
  open lateinit var streamId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "streamId", referencedColumnName = "id")
  open var stream: StreamEntity? = null
}

