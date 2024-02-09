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
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.validation.constraints.Size
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type
import org.migor.feedless.api.graphql.DtoResolver.toDto
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.generated.types.DiffEmailForwardParams
import org.migor.feedless.generated.types.DiffEmailForwardParamsInput
import org.migor.feedless.generated.types.FulltextPluginParams
import org.migor.feedless.generated.types.FulltextPluginParamsInput
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.PluginExecutionParams
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.Retention
import org.migor.feedless.generated.types.Selectors
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.generated.types.SourceSubscription
import java.util.*

data class PluginRef(val id: String, val params: PluginExecutionParamsInput?)

@Entity
@Table(name = "t_source_subscription")
open class SourceSubscriptionEntity : EntityWithUUID() {

  @Basic
  @Column(name = StandardJpaFields.title, nullable = false)
  @Size(min = 3, max = 50)
  open lateinit var title: String

  @Basic
  @Column(name = StandardJpaFields.description, nullable = false, length = 1024)
  open lateinit var description: String

  @Basic
  @Column(name = StandardJpaFields.visibility, nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  open var visibility: EntityVisibility = EntityVisibility.isPublic

  @Basic
  @Column(nullable = false)
  open lateinit var schedulerExpression: String

  @Basic
  open var tag: String? = null

  @Basic
  open var retentionMaxItems: Int? = null

  @Basic
  open var retentionMaxAgeDays: Int? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var lastUpdatedAt: Date? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var disabledFrom: Date? = null

  @Column(nullable = false)
  open var archived: Boolean = false

  @Column(nullable = false)
  open lateinit var product: ProductName

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

  @OneToMany(fetch = FetchType.LAZY, mappedBy = StandardJpaFields.subscriptionId)
  open var mailForwards: MutableList<MailForwardEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = StandardJpaFields.subscriptionId)
  open var documents: MutableList<WebDocumentEntity> = mutableListOf()

  @Basic
  @Column(name = "segmentation_id", insertable = false, updatable = false)
  open var segmentationId: UUID? = null

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(name = "segmentation_id", referencedColumnName = "id", foreignKey = ForeignKey(name = "fk_source_subscription__segmentation"))
  open var segmentation: SegmentationEntity? = null

  @PrePersist
  fun prePersist() {
//    this.archived = disabledFrom != null
  }
}

fun SourceSubscriptionEntity.toDto(): SourceSubscription {
  return SourceSubscription.newBuilder()
    .id(id.toString())
    .ownerId(ownerId.toString())
    .disabledFrom(disabledFrom?.time)
    .plugins(plugins.map { it.toDto() })
    .archived(archived)
    .retention(Retention.newBuilder()
      .maxItems(retentionMaxItems)
      .maxAgeDays(retentionMaxAgeDays)
      .build())
    .visibility(visibility.toDto())
    .createdAt(createdAt.time)
    .description(description)
    .title(title)
    .segmented(segmentation?.toDto())
    .sources(sources.map { it.toDto() })
    .build()
}

private fun PluginRef.toDto(): PluginExecution {
  return PluginExecution.newBuilder()
    .pluginId(id)
    .params(params?.toDto())
    .build()
}

private fun PluginExecutionParamsInput.toDto(): PluginExecutionParams {
  return PluginExecutionParams.newBuilder()
    .fulltext(fulltext?.toDto())
    .genericFeed(genericFeed?.toDto())
    .diffEmailForward(diffEmailForward?.toDto())
    .rawJson(rawJson)
    .build()
}

private fun DiffEmailForwardParamsInput.toDto(): DiffEmailForwardParams {
  return DiffEmailForwardParams.newBuilder()
    .compareBy(compareBy)
    .nextItemMinIncrement(nextItemMinIncrement)
    .inlineDiffImage(inlineDiffImage)
    .inlineLatestImage(inlineLatestImage)
    .inlinePreviousImage(inlinePreviousImage)
    .build()
}

private fun SelectorsInput.toDto(): Selectors {
  return Selectors.newBuilder()
    .contextXPath(contextXPath)
    .dateXPath(dateXPath)
    .extendContext(extendContext)
    .linkXPath(linkXPath)
    .dateIsStartOfEvent(dateIsStartOfEvent)
    .build()
}

private fun FulltextPluginParamsInput.toDto(): FulltextPluginParams {
  return FulltextPluginParams.newBuilder()
    .readability(readability)
    .build()
}

