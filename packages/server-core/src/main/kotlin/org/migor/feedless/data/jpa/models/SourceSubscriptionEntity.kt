package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import java.util.*

@Entity
@Table(name = "t_source_subscription")
open class SourceSubscriptionEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false)
  open lateinit var schedulerExpression: String

  @Basic
  open var retentionSize: Int? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var lastUpdatedAt: Date? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var triggerScheduledNextAt: Date? = null

  @Basic
  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY, cascade = [])
  @JoinColumn(name = StandardJpaFields.ownerId, referencedColumnName = StandardJpaFields.id, insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_native_feed__user"))
  open var owner: UserEntity? = null

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], mappedBy = StandardJpaFields.subscriptionId)
  open var sources: MutableList<ScrapeSourceEntity> = mutableListOf()

//  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], mappedBy = StandardJpaFields.subscriptionId)
//  open var sink: MutableList<ScrapeSourceEntity> = mutableListOf()

//  @OneToOne(mappedBy = "stream", cascade = [CascadeType.REMOVE], optional = true)
//  open var bucket: BucketEntity? = null

  @Basic
  @Column(name = "bucketId", nullable = false)
  open lateinit var bucketId: UUID

  @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
  @JoinColumn(name = "bucketId", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_subscription__bucket"))
  open var bucket: BucketEntity? = null


}

