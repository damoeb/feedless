package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
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

@Entity
@Table(name = "t_article")
open class ArticleEntity : EntityWithUUID() {

  @Basic
  @Column(name = "date_released", nullable = true)
  open var releasedAt: Date? = null

  @Basic
  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  open var status: ReleaseStatus = ReleaseStatus.released

  @Basic
  @Column(name = "contentId", nullable = false, insertable = false, updatable = false)
  open lateinit var contentId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contentId", referencedColumnName = "id")
  open var content: ContentEntity? = null

  @Basic
  @Column(name = "streamId", nullable = false, insertable = false, updatable = false)
  open lateinit var streamId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "streamId", referencedColumnName = "id")
  open var stream: StreamEntity? = null

  @Basic
  @Column(name = "feedId", nullable = false, insertable = false, updatable = false)
  open lateinit var feedId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feedId", referencedColumnName = "id")
  open var feed: NativeFeedEntity? = null

//  @Basic
//  @Column(name = "ownerId", nullable = false, insertable = false, updatable = false)
//  open lateinit var ownerId: UUID
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "ownerId", referencedColumnName = "id")
//  open var owner: UserEntity? = null

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  open var type: ArticleType = ArticleType.feed
}

