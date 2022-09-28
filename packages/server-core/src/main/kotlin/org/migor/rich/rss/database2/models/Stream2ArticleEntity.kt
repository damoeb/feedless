package org.migor.rich.rss.database2.models

import org.migor.rich.rss.database2.EntityWithUUID
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull

enum class ArticleType(val id: Int) {
  feed(0),
  ops(1),
  note(2),
  digest(3);

  companion object {
    fun findById(id: Int?): ArticleType? {
      return values().find { otherType -> otherType.id == id }
    }

    fun findByName(type: String?): ArticleType? {
      return values().find { otherType -> otherType.name == type?.lowercase() }
    }
  }
}



@Entity
@Table(name = "map_stream_to_article")
open class Stream2ArticleEntity : EntityWithUUID() {

  @Basic
  @Column(name = "date_released", nullable = false)
  open var releasedAt: Date? = null

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

  @Basic
  @Column(name = "ownerId", nullable = false, insertable = false, updatable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ownerId", referencedColumnName = "id")
  open var owner: UserEntity? = null

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "\"type\"")
  open var type: ArticleType = ArticleType.feed
}

