package org.migor.feedless.data.jpa.models

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
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
import org.hibernate.annotations.UpdateTimestamp
import org.migor.feedless.api.graphql.DtoResolver.toDto
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.generated.types.CompositeFieldFilterParams
import org.migor.feedless.generated.types.CompositeFieldFilterParamsInput
import org.migor.feedless.generated.types.CompositeFilterParams
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.DiffEmailForwardParams
import org.migor.feedless.generated.types.DiffEmailForwardParamsInput
import org.migor.feedless.generated.types.FulltextPluginParams
import org.migor.feedless.generated.types.FulltextPluginParamsInput
import org.migor.feedless.generated.types.NumericalFilterParams
import org.migor.feedless.generated.types.NumericalFilterParamsInput
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.PluginExecutionParams
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.Retention
import org.migor.feedless.generated.types.Selectors
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.generated.types.SourceSubscription
import org.migor.feedless.generated.types.StringFilterParams
import org.migor.feedless.generated.types.StringFilterParamsInput
import org.springframework.context.annotation.Lazy
import java.util.*

data class PluginExecution(val id: String, val params: PluginExecutionParamsInput)

@Entity
@Table(name = "t_source_subscription")
open class SourceSubscriptionEntity : EntityWithUUID() {

  @Column(name = StandardJpaFields.title, nullable = false)
  @Size(min = 3, max = 50)
  open lateinit var title: String

  @Column(name = StandardJpaFields.description, nullable = false, length = 1024)
  open lateinit var description: String

  @Column(name = StandardJpaFields.visibility, nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  open var visibility: EntityVisibility = EntityVisibility.isPublic

  @Column(nullable = false)
  open lateinit var schedulerExpression: String

  open var tag: String? = null

  open var retentionMaxItems: Int? = null

  open var retentionMaxAgeDays: Int? = null

  @Temporal(TemporalType.TIMESTAMP)
  @UpdateTimestamp
  @Column
  open var lastUpdatedAt: Date = Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var disabledFrom: Date? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var sunsetAfterTimestamp: Date? = null

  @Column
  open var sunsetAfterTotalDocumentCount: Int? = null

  @Column
  open var documentCountSinceCreation: Int = 0

  @Column(nullable = false)
  open var archived: Boolean = false

  @Column(nullable = false)
  @Deprecated("comes from user")
  open lateinit var product: ProductName

  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb", nullable = false)
  @Lazy
  open var plugins: List<org.migor.feedless.data.jpa.models.PluginExecution> = emptyList()

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var triggerScheduledNextAt: Date? = null

  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.ownerId,
    referencedColumnName = StandardJpaFields.id,
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_native_feed__user")
  )
  open var owner: UserEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = StandardJpaFields.subscriptionId)
  open var sources: MutableList<ScrapeSourceEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = StandardJpaFields.subscriptionId)
  open var mailForwards: MutableList<MailForwardEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = StandardJpaFields.subscriptionId)
  open var documents: MutableList<WebDocumentEntity> = mutableListOf()

  @Column(name = "segmentation_id", insertable = false, updatable = false)
  open var segmentationId: UUID? = null

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(
    name = "segmentation_id",
    referencedColumnName = "id",
    foreignKey = ForeignKey(name = "fk_source_subscription__segmentation")
  )
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
    .retention(
      Retention.newBuilder()
        .maxItems(retentionMaxItems)
        .maxAgeDays(retentionMaxAgeDays)
        .build()
    )
    .visibility(visibility.toDto())
    .createdAt(createdAt.time)
    .updatedAt(lastUpdatedAt.time)
    .description(description)
    .title(title)
    .segmented(segmentation?.toDto())
    .scheduleExpression(schedulerExpression)
    .build()
}

private fun org.migor.feedless.data.jpa.models.PluginExecution.toDto(): PluginExecution {
  return PluginExecution.newBuilder()
    .pluginId(id)
    .params(params.toDto())
    .build()
}

private fun PluginExecutionParamsInput.toDto(): PluginExecutionParams {
  return PluginExecutionParams.newBuilder()
    .org_feedless_fulltext(org_feedless_fulltext?.toDto())
    .org_feedless_feed(org_feedless_feed?.toDto())
    .org_feedless_diff_email_forward(org_feedless_diff_email_forward?.toDto())
    .jsonData(jsonData)
    .org_feedless_filter(org_feedless_filter?.map { it.toDto() })
    .build()
}

private fun CompositeFilterParamsInput.toDto(): CompositeFilterParams {
  return CompositeFilterParams.newBuilder()
    .include(include?.toDto())
    .exclude(exclude?.toDto())
    .build()
}

private fun CompositeFieldFilterParamsInput.toDto(): CompositeFieldFilterParams {
  return CompositeFieldFilterParams.newBuilder()
    .index(index?.toDto())
    .title(title?.toDto())
    .content(content?.toDto())
    .link(link?.toDto())
    .build()
}

private fun NumericalFilterParamsInput.toDto(): NumericalFilterParams {
  return NumericalFilterParams.newBuilder()
    .value(value)
    .operator(operator)
    .build()
}

private fun StringFilterParamsInput.toDto(): StringFilterParams {
  return StringFilterParams.newBuilder()
    .value(value)
    .operator(operator)
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

