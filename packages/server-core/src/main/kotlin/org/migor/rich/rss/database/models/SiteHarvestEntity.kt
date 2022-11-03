package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "t_site_harvest")
open class SiteHarvestEntity : EntityWithUUID() {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feedId", referencedColumnName = "id")
  open var feed: NativeFeedEntity? = null

  @Column(name = "feedId", insertable = false, updatable = false, nullable = false)
  open var feedId: UUID? = null

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "articleId", referencedColumnName = "id")
  open var article: ArticleContentEntity? = null

  @Column(name = "articleId", insertable = false, updatable = false, nullable = false, unique = true)
  open var articleId: UUID? = null

  @Basic
  @Column(name = "error_count", nullable = false)
  open var errorCount: Int = 0

  @Basic
  @Column(name = "error_message", columnDefinition = "TEXT")
  open var errorMessage: String? = null

  @Basic
  @Column(name = "last_attempt_at")
  open var lastAttemptAt: Date? = null

  @Basic
  @Column(name = "next_attempt_after")
  open var nextAttemptAfter: Date? = null

}

