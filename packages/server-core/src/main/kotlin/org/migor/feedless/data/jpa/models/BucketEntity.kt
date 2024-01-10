package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
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

//  @Type(StringArrayType::class)
//  @Column(name = StandardJpaFields.tags, length = 200, columnDefinition = "text[]")
//  open var tags: Array<String>? = null

  // todo remove
  @Basic
  @Column(name = StandardJpaFields.visibility, nullable = false)
  @Enumerated(EnumType.STRING)
  open var visibility: EntityVisibility = EntityVisibility.isPublic

  // todo remove
  @Basic
  @Column
  open var lastUpdatedAt: Date? = null

  @Basic
  @Column(name = "streamId", nullable = false)
  open lateinit var streamId: UUID

  @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
  @JoinColumn(name = "streamId", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_bucket__stream"))
  open var stream: StreamEntity? = null

  @Basic
  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY, cascade = [])
  @JoinColumn(name = StandardJpaFields.ownerId, referencedColumnName = "id", insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_bucket__user"))
  open var owner: UserEntity? = null

  @OneToMany(fetch = FetchType.LAZY, cascade = [], mappedBy = "bucketId")
  open var importers: MutableList<ImporterEntity> = mutableListOf()

}

