package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import java.util.*
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "t_hyperlink", indexes = [
  Index(name = "idx_hyperlink_url", columnList = "fromId")
])
open class HyperLinkEntity: EntityWithUUID() {

  @Basic
  @Column(nullable = false)
  open var relevance: Double = 0.0

  @Basic
  @Column(nullable = false)
  open var hyperText: String? = null

  @Basic
  @Column(name = "fromId", nullable = false, insertable = false, updatable = false)
  open lateinit var fromId: UUID

  @ManyToOne(fetch = FetchType.LAZY, cascade = [])
  @JoinColumn(name = "fromId", referencedColumnName = "id")
  open var from: ContentEntity? = null

  @Basic
  @Column(name = "toId", nullable = false, insertable = false, updatable = false)
  open lateinit var toId: UUID

  @ManyToOne(fetch = FetchType.LAZY, cascade = [])
  @JoinColumn(name = "toId", referencedColumnName = "id")
  open var to: WebDocumentEntity? = null

}
