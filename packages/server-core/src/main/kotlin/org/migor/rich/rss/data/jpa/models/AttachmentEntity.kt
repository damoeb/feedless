package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.migor.rich.rss.data.jpa.EntityWithUUID
import java.util.*

@Entity
@Table(name = "t_attachment")
open class AttachmentEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false, length = 1000)
  open lateinit var url: String

  @Basic
  @NotNull
  open var mimeType: String? = null

  @Basic
  open var size: Long? = null

  @Basic
  open var duration: Long? = null

  @Basic
  @Column(name = "contentId", nullable = false, insertable = false, updatable = false)
  open var contentId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contentId", referencedColumnName = "id")
  open var content: ContentEntity? = null
}

