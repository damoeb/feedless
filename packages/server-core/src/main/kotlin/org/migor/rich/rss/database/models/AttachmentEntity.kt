package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "t_attachment")
open class AttachmentEntity : EntityWithUUID() {

  @Basic
  @Column(name = "url", nullable = false, length = 1000)
  open var url: String? = null

  @Basic
  @NotNull
  open var mimeType: String? = null

  @Basic
  open var length: Long? = null

  @Basic
  @Column(name = "contentId", nullable = false, insertable = false, updatable = false)
  open var contentId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [])
  @JoinColumn(name = "contentId", referencedColumnName = "id")
  open var content: ContentEntity? = null
}

