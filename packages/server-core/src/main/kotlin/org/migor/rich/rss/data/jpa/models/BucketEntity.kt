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
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.migor.rich.rss.data.jpa.EntityWithUUID
import org.migor.rich.rss.data.jpa.StandardJpaFields
import org.migor.rich.rss.data.jpa.enums.EntityVisibility
import java.util.*

@Entity
@Table(name = "t_bucket")
open class BucketEntity : EntityWithUUID() {

  @Basic
  @Column(name = StandardJpaFields.title, nullable = false)
  open lateinit var title: String

  @Basic
  @Column(name = StandardJpaFields.description, nullable = false, length = 1024)
  open lateinit var description: String

  @Basic
  @Column(name = StandardJpaFields.websiteUrl, length = 200)
  open var websiteUrl: String? = null

  @Basic
  @Column(length = 200)
  open var webhookUrl: String? = null

  @Basic
  @Column(nullable = false)
  open var archive = false

//  @Basic
//  @Column(name = StandardJpaFields.websiteUrl, length = 200)
//  open var emails: Boolean? = null

  @Basic
  @Column(name = StandardJpaFields.imageUrl, length = 200)
  open var imageUrl: String? = null

  @Basic
  @Column(name = StandardJpaFields.tags, length = 200)
  open var tags: String? = null

  @Basic
  @Column(name = StandardJpaFields.visibility, nullable = false)
  @Enumerated(EnumType.STRING)
  open var visibility: EntityVisibility = EntityVisibility.isPublic

  @Basic
  @Column
  open var lastUpdatedAt: Date? = null

  @Basic
  @Column(name = "streamId", nullable = false)
  open lateinit var streamId: UUID

  @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
  @JoinColumn(name = "streamId", referencedColumnName = "id", insertable = false, updatable = false)
  open var stream: StreamEntity? = null

  @Basic
  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = StandardJpaFields.ownerId, referencedColumnName = "id", insertable = false, updatable = false)
  open var owner: UserEntity? = null

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], orphanRemoval = true, mappedBy = "bucketId")
  open var importers: MutableList<ImporterEntity> = mutableListOf()

}

