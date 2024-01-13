package org.migor.feedless.data.jpa.models

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Basic
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
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type
import org.migor.feedless.api.graphql.DtoResolver.toDto
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.generated.types.Retention
import org.migor.feedless.generated.types.SourceSubscription
import java.util.*

data class PluginRef(val id: String, val params: String?)

@Entity
@Table(name = "t_source_subscription")
open class SourceSubscriptionEntity : EntityWithUUID() {

  @Basic
  @Column(name = StandardJpaFields.title, nullable = false)
  open lateinit var title: String

  @Basic
  @Column(name = StandardJpaFields.description, nullable = false, length = 1024)
  open lateinit var description: String

  @Basic
  @Column(name = StandardJpaFields.visibility, nullable = false)
  @Enumerated(EnumType.STRING)
  open var visibility: EntityVisibility = EntityVisibility.isPublic

  @Basic
  @Column(nullable = false)
  open lateinit var schedulerExpression: String

  @Basic
  open var retentionMaxItems: Int? = null

  @Basic
  open var retentionMaxAgeDays: Int? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var lastUpdatedAt: Date? = null

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", nullable = false)
  @Basic(fetch = FetchType.LAZY)
  open var plugins: List<PluginRef> = emptyList()

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var triggerScheduledNextAt: Date? = null

  @Basic
  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = StandardJpaFields.ownerId, referencedColumnName = StandardJpaFields.id, insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_native_feed__user"))
  open var owner: UserEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = StandardJpaFields.subscriptionId)
  open var sources: MutableList<ScrapeSourceEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY)
  open var documents: MutableList<WebDocumentEntity> = mutableListOf()

  @Basic
  @Column(name = "segmentation_id", insertable = false, updatable = false)
  open var segmentationId: UUID? = null

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(name = "segmentation_id", referencedColumnName = "id", foreignKey = ForeignKey(name = "fk_source_subscription__segmentation"))
  open var segmentation: SegmentationEntity? = null
}

fun SourceSubscriptionEntity.toDto(): SourceSubscription {
  return SourceSubscription.newBuilder()
    .id(this.id.toString())
    .ownerId(this.ownerId.toString())
    .retention(Retention.newBuilder()
      .maxItems(this.retentionMaxItems)
      .maxAgeDays(this.retentionMaxAgeDays)
      .build())
    .visibility(this.visibility.toDto())
    .createdAt(this.createdAt.time)
    .description(this.description)
    .title(this.title)
    .segmented(this.segmentation?.toDto())
    .sources(this.sources.map { it.toDto() })
    .build()
}

