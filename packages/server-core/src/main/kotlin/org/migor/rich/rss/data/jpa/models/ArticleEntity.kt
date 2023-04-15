package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.migor.rich.rss.data.jpa.EntityWithUUID
import org.migor.rich.rss.data.jpa.StandardJpaFields
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import java.util.*

@Entity
@Table(name = "t_article")
open class ArticleEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = true, name = StandardJpaFields.releasedAt)
  open lateinit var releasedAt: Date

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open var status: ReleaseStatus = ReleaseStatus.released

  @Basic
  @Column(name = "contentId", nullable = false, insertable = false, updatable = false)
  open lateinit var contentId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contentId", referencedColumnName = "id")
  open var content: ContentEntity? = null

  @Basic
  @Column(name = "streamId", nullable = false)
  open lateinit var streamId: UUID

//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "streamId", referencedColumnName = "id")
//  open var stream: StreamEntity? = null

//  @Basic
//  @Column(name = "feedId", nullable = false, insertable = false, updatable = false)
//  open lateinit var feedId: UUID
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "feedId", referencedColumnName = "id")
//  open var feed: NativeFeedEntity? = null

//  @Basic
//  @Column(name = StandardJpaFields.ownerId, nullable = false, insertable = false, updatable = false)
//  open lateinit var ownerId: UUID
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = StandardJpaFields.ownerId, referencedColumnName = "id")
//  open var owner: UserEntity? = null

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  open var type: ArticleType = ArticleType.feed
}

