package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import org.migor.rich.rss.database.enums.BucketVisibility
import java.util.*
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "t_bucket")
open class BucketEntity : EntityWithUUID() {

  @Basic
  @Column(name = "name", nullable = false)
  open var name: String? = null

  @Basic
  @Column(name = "filter")
  open var filter: String? = null

  @Basic
  @Column(name = "description", nullable = false, length = 1024)
  open var description: String? = null

  @Basic
  @Column(name = "website_url", length = 200)
  open var websiteUrl: String? = null

  @Basic
  @Column(name = "visibility", nullable = false)
  @Enumerated(EnumType.STRING)
  open var visibility: BucketVisibility = BucketVisibility.public

  @Basic
  @Column(name = "lastUpdatedAt")
  open var lastUpdatedAt: Date? = null

  @Basic
  @Column(name = "streamId", nullable = false, insertable = false, updatable = false)
  open var streamId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "streamId", referencedColumnName = "id")
  open var stream: StreamEntity? = null

  @Basic
  @Column(name = "ownerId", nullable = false, insertable = false, updatable = false)
  open var ownerId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ownerId", referencedColumnName = "id")
  open var owner: UserEntity? = null

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "bucketId")
  open var importers: MutableList<ImporterEntity> = mutableListOf()

}

