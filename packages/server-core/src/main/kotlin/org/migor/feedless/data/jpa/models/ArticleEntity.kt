package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ArticleType
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import java.util.*

@Entity
@Table(name = "t_article")
open class ArticleEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = true, name = StandardJpaFields.releasedAt)
  open lateinit var releasedAt: Date

  @Basic
  @Column(nullable = false, name = StandardJpaFields.status)
  @Enumerated(EnumType.STRING)
  open var status: ReleaseStatus = ReleaseStatus.released

  @Basic
  @Column(name = StandardJpaFields.webDocumentId, nullable = false)
  open lateinit var webDocumentId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = StandardJpaFields.webDocumentId, referencedColumnName = "id", insertable = false, updatable = false)
  open var webDocument: WebDocumentEntity? = null

  @Basic
  @Column(name = "streamId", nullable = false)
  open lateinit var streamId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "streamId", referencedColumnName = "id", insertable = false, updatable = false)
  open var stream: StreamEntity? = null

  @Basic
  @Column(name = "importerId")
  open var importerId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "importerId", referencedColumnName = "id", insertable = false, updatable = false)
  open var importer: ImporterEntity? = null

  @Basic
  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = StandardJpaFields.ownerId, referencedColumnName = "id", insertable = false, updatable = false)
  open var owner: UserEntity? = null

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = StandardJpaFields.type, nullable = false)
  open var type: ArticleType = ArticleType.feed
}

