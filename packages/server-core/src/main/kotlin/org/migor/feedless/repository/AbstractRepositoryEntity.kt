package org.migor.feedless.repository

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.validation.constraints.Size
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import org.migor.feedless.api.toDto
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.data.jpa.models.SegmentationEntity
import org.migor.feedless.data.jpa.models.SourceEntity
import org.migor.feedless.data.jpa.models.toDto
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.CompareBy
import org.migor.feedless.generated.types.CompareByInput
import org.migor.feedless.generated.types.CompositeFieldFilterParams
import org.migor.feedless.generated.types.CompositeFieldFilterParamsInput
import org.migor.feedless.generated.types.CompositeFilterParams
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.DiffEmailForwardParams
import org.migor.feedless.generated.types.DiffEmailForwardParamsInput
import org.migor.feedless.generated.types.FeedParams
import org.migor.feedless.generated.types.FeedParamsInput
import org.migor.feedless.generated.types.FulltextPluginParams
import org.migor.feedless.generated.types.FulltextPluginParamsInput
import org.migor.feedless.generated.types.NumericalFilterParams
import org.migor.feedless.generated.types.NumericalFilterParamsInput
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.PluginExecutionParams
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.ProductCategory as ProductCategoryDto
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.Retention
import org.migor.feedless.generated.types.Selectors
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.generated.types.StringFilterParams
import org.migor.feedless.generated.types.StringFilterParamsInput
import org.migor.feedless.mail.MailForwardEntity
import org.migor.feedless.user.UserEntity
import org.springframework.context.annotation.Lazy
import java.util.*

data class PluginExecution(val id: String, val params: PluginExecutionParamsInput)

@Entity
@Table(name = "t_repository")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
  name = "type",
  discriminatorType = DiscriminatorType.STRING
)
open class AbstractRepositoryEntity : EntityWithUUID() {

  @Column(name = StandardJpaFields.title, nullable = false)
  @Size(min = 3, max = 50)
  open lateinit var title: String

  @Column(name = StandardJpaFields.description, nullable = false, length = 1024)
  open lateinit var description: String

  @Column(name = StandardJpaFields.visibility, nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  open var visibility: EntityVisibility = EntityVisibility.isPublic

  @Column(nullable = false, name = "scheduler_expression")
  open lateinit var sourcesSyncExpression: String

  @Column(name = "retention_max_items")
  open var retentionMaxItems: Int? = null

  @Column(name = "retention_max_age_days")
  open var retentionMaxAgeDays: Int? = null

  @Temporal(TemporalType.TIMESTAMP)
  @UpdateTimestamp
  @Column(name = "last_updated_at")
  open var lastUpdatedAt: Date = Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "disabled_from")
  open var disabledFrom: Date? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "sunset_after")
  open var sunsetAfterTimestamp: Date? = null

  @Column(name = "sunset_after_total_document_count")
  open var sunsetAfterTotalDocumentCount: Int? = null

  @Column(name = "document_count_since_creation")
  open var documentCountSinceCreation: Int = 0

  @Column(nullable = false, name = "is_archived")
  open var archived: Boolean = false

  @Column(nullable = false, name = "for_product")
  open lateinit var product: ProductCategory

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "trigger_scheduled_next_at")
  open var triggerScheduledNextAt: Date? = null

  @Column(nullable = false, name = "schema_version")
  open var schemaVersion: Int = 0

  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb", nullable = false, name = "plugins")
  @Lazy
  open var plugins: List<org.migor.feedless.repository.PluginExecution> = emptyList()

  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.ownerId,
    referencedColumnName = StandardJpaFields.id,
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_repository__to__user")
  )
  open var owner: UserEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "repositoryId", cascade = [CascadeType.ALL])
  open var sources: MutableList<SourceEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "repositoryId")
  open var mailForwards: MutableList<MailForwardEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "repositoryId")
  open var documents: MutableList<DocumentEntity> = mutableListOf()

  @Column(name = "segmentation_id", insertable = false, updatable = false)
  open var segmentationId: UUID? = null

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(
    name = "segmentation_id",
    referencedColumnName = "id",
    foreignKey = ForeignKey(name = "fk_repository__to__segmentation")
  )
  open var segmentation: SegmentationEntity? = null
}

fun RepositoryEntity.toDto(): Repository {
  return Repository.newBuilder()
    .id(id.toString())
    .ownerId(ownerId.toString())
    .product(product.toDto())
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
    .lastUpdatedAt(lastUpdatedAt.time)
    .nextUpdateAt(triggerScheduledNextAt?.time)
    .description(description)
    .title(title)
    .segmented(segmentation?.toDto())
    .refreshCron(sourcesSyncExpression)
    .build()
}

private fun ProductCategory.toDto(): ProductCategoryDto {
  return when(this) {
    ProductCategory.rssProxy -> ProductCategoryDto.rssProxy
    ProductCategory.visualDiff -> ProductCategoryDto.visualDiff
    else -> throw IllegalArgumentException("Unsupported product name: $this")
  }
}

private fun org.migor.feedless.repository.PluginExecution.toDto(): PluginExecution {
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

private fun FeedParamsInput.toDto(): FeedParams {
  return FeedParams.newBuilder()
    .generic(generic?.toDto())
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
    .compareBy(compareBy.toDto())
    .nextItemMinIncrement(nextItemMinIncrement)
    .inlineDiffImage(inlineDiffImage)
    .inlineLatestImage(inlineLatestImage)
    .inlinePreviousImage(inlinePreviousImage)
    .build()
}

private fun CompareByInput.toDto(): CompareBy {
  return CompareBy.newBuilder()
    .field(field)
    .fragmentNameRef(fragmentNameRef)
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

