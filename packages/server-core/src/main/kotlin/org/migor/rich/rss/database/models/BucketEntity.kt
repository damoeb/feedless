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

@Entity
@Table(name = "t_bucket")
open class BucketEntity : EntityWithUUID() {

  @Basic
  @Column(name = "name", nullable = false)
  open var name: String? = null

  @Basic
  @Column(name = "description", length = 1024)
  open var description: String? = null

  @Basic
  @Column(name = "is_release_manually", nullable = false)
  open var isReleaseManually: Boolean = false

  @Basic
  @Column(name = "visibility", nullable = false)
  @Enumerated(EnumType.STRING)
  open var visibility: BucketVisibility = BucketVisibility.public

  @Basic
  @Column(name = "lastUpdatedAt")
  open var lastUpdatedAt: java.sql.Timestamp? = null

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
  open var subscriptions: MutableList<Subscription> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "bucketId")
  open var exporters: MutableList<ExporterEntity> = mutableListOf()

}

