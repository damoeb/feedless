package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.migor.rich.rss.data.jpa.EntityWithUUID
import java.util.*

@Entity
@Table(name = "t_harvest_task")
open class HarvestTaskEntity : EntityWithUUID() {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feedId", referencedColumnName = "id")
  open var feed: NativeFeedEntity? = null

  @Column(name = "feedId", insertable = false, updatable = false)
  open var feedId: UUID? = null

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contentId", referencedColumnName = "id")
  open var content: ContentEntity? = null

  @Column(name = "contentId", insertable = false, updatable = false)
  open var contentId: UUID? = null

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "webDocumentId", referencedColumnName = "id")
  open var webDocument: WebDocumentEntity? = null

  @Column(name = "webDocumentId", insertable = false, updatable = false)
  open var webDocumentId: UUID? = null

  @Basic
  @Column(nullable = false)
  open var errorCount: Int = 0

  @Basic
  @Column(columnDefinition = "TEXT")
  open var errorMessage: String? = null

  @Basic
  @Column
  open var lastAttemptAt: Date? = null

  @Basic
  @Column(name = "next_attempt_after")
  open var nextAttemptAfter: Date? = null

}

