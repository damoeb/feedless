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
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.toDto
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.data.jpa.enums.toDto
import org.migor.feedless.data.jpa.models.SegmentationEntity
import org.migor.feedless.data.jpa.models.toDto
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentEntity.Companion.LEN_STR_DEFAULT
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
import org.migor.feedless.generated.types.ItemFilterParams
import org.migor.feedless.generated.types.ItemFilterParamsInput
import org.migor.feedless.generated.types.NumericalFilterParams
import org.migor.feedless.generated.types.NumericalFilterParamsInput
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.PluginExecutionParams
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.Retention
import org.migor.feedless.generated.types.StringFilterParams
import org.migor.feedless.generated.types.StringFilterParamsInput
import org.migor.feedless.generated.types.WebDocumentDateField
import org.migor.feedless.mail.MailForwardEntity
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.user.UserEntity
import org.springframework.context.annotation.Lazy
import java.util.*

data class PluginExecution(val id: String, val params: PluginExecutionParamsInput)

enum class MaxAgeDaysDateField {
  createdAt,
  startingAt,
  publishedAt
}


@Entity
@Table(name = "t_repository")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
  name = "type",
  discriminatorType = DiscriminatorType.STRING
)
open class AbstractRepositoryEntity : EntityWithUUID() {

  @Column(name = StandardJpaFields.title, nullable = false)
  @Size(min = 3, max = LEN_STR_DEFAULT)
  open lateinit var title: String

  @Size(max = 1024)
  @Column(name = StandardJpaFields.description, nullable = false, length = 1024)
  open lateinit var description: String

  @Column(name = StandardJpaFields.visibility, nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  open var visibility: EntityVisibility = EntityVisibility.isPublic

  @Size(max = LEN_STR_DEFAULT)
  @Column(nullable = false, name = "scheduler_expression")
  open lateinit var sourcesSyncCron: String

  @Column(name = "retention_max_items")
  open var retentionMaxCapacity: Int? = null

  @Column(name = "retention_max_age_days")
  open var retentionMaxAgeDays: Int? = null

  @Enumerated(EnumType.STRING)
  @Column(name = "retention_max_age_days_field", nullable = false, length = 50)
  open var retentionMaxAgeDaysReferenceField: MaxAgeDaysDateField = MaxAgeDaysDateField.publishedAt

  @Temporal(TemporalType.TIMESTAMP)
  @UpdateTimestamp
  @Column(name = "last_updated_at")
  open var lastUpdatedAt: Date = Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "disabled_from")
  open var disabledFrom: Date? = null

  @Size(max = 10)
  @Column(name = "share_key", nullable = false, length = 10)
  open var shareKey: String = ""

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "sunset_after")
  open var sunsetAfterTimestamp: Date? = null

  @Column(name = "sunset_after_total_document_count")
  open var sunsetAfterTotalDocumentCount: Int? = null

  @Column(name = "document_count_since_creation", nullable = false)
  open var documentCountSinceCreation: Int = 0

  @Column(nullable = false, name = "is_archived")
  open var archived: Boolean = false

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "for_product", length = 20)
  open lateinit var product: ProductCategory

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "trigger_scheduled_next_at")
  open var triggerScheduledNextAt: Date? = null

  @Column(nullable = false, name = "schema_version")
  open var schemaVersion: Int = 0

  @Type(JsonBinaryType::class)
  @Lazy
  @Column(columnDefinition = "jsonb", nullable = false, name = "plugins")
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
  return Repository(
    id = id.toString(),
    ownerId = ownerId.toString(),
    product = product.toDto(),
    disabledFrom = disabledFrom?.time,
    plugins = plugins.map { it.toDto() },
    archived = archived,
    retention =
    Retention(
      maxCapacity = retentionMaxCapacity,
      maxAgeDays = retentionMaxAgeDays
    ),
    shareKey = shareKey,
    visibility = visibility.toDto(),
    createdAt = createdAt.time,
    lastUpdatedAt = lastUpdatedAt.time,
    nextUpdateAt = triggerScheduledNextAt?.time,
    description = description,
    title = title,
    segmented = segmentation?.toDto(),
    refreshCron = sourcesSyncCron,
    documentCount = 0,
    tags = emptyList(),
    cronRuns = emptyList()
  )
}

private fun org.migor.feedless.repository.PluginExecution.toDto(): PluginExecution {
  return PluginExecution(
    pluginId = id,
    params = params.toDto(),
  )
}

fun PluginExecutionParamsInput.toDto(): PluginExecutionParams {
  return PluginExecutionParams(
    org_feedless_fulltext = org_feedless_fulltext?.toDto(),
    org_feedless_feed = org_feedless_feed?.toDto(),
    org_feedless_diff_email_forward = org_feedless_diff_email_forward?.toDto(),
    jsonData = jsonData,
    org_feedless_filter = org_feedless_filter?.map { it.toDto() },
  )
}

private fun ItemFilterParamsInput.toDto(): ItemFilterParams {
  return ItemFilterParams(
    composite = composite?.toDto(),
    expression = expression
  )
}

fun FeedParamsInput.toDto(): FeedParams {
  return FeedParams(
    generic = generic?.fromDto(),
  )
}

fun CompositeFilterParamsInput.toDto(): CompositeFilterParams {
  return CompositeFilterParams(
    include = include?.toDto(),
    exclude = exclude?.toDto(),
  )
}

fun CompositeFieldFilterParamsInput.toDto(): CompositeFieldFilterParams {
  return CompositeFieldFilterParams(
    index = index?.toDto(),
    title = title?.toDto(),
    content = content?.toDto(),
    link = link?.toDto(),
  )
}

private fun NumericalFilterParamsInput.toDto(): NumericalFilterParams {
  return NumericalFilterParams(
    value = value,
    operator = operator,
  )
}

private fun StringFilterParamsInput.toDto(): StringFilterParams {
  return StringFilterParams(
    value = value,
    operator = operator,
  )
}

private fun DiffEmailForwardParamsInput.toDto(): DiffEmailForwardParams {
  return DiffEmailForwardParams(
    compareBy = compareBy.toDto(),
    nextItemMinIncrement = nextItemMinIncrement,
    inlineDiffImage = inlineDiffImage,
    inlineLatestImage = inlineLatestImage,
    inlinePreviousImage = inlinePreviousImage,
  )
}

private fun CompareByInput.toDto(): CompareBy {
  return CompareBy(
    field = field,
    fragmentNameRef = fragmentNameRef,
  )
}

private fun FulltextPluginParamsInput.toDto(): FulltextPluginParams {
  return FulltextPluginParams(
    readability = readability,
    inheritParams = false
  )
}

fun WebDocumentDateField.fromDto(): MaxAgeDaysDateField {
  return when(this) {
    WebDocumentDateField.createdAt -> MaxAgeDaysDateField.createdAt
    WebDocumentDateField.startingAt -> MaxAgeDaysDateField.startingAt
    WebDocumentDateField.publishedAt -> MaxAgeDaysDateField.publishedAt
  }
}

