package org.migor.feedless.repository

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
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.validation.constraints.Size
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.type.SqlTypes
import org.migor.feedless.actions.PluginExecutionJsonEntity
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.toDto
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.data.jpa.enums.toDto
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
import org.migor.feedless.generated.types.RecordDateField
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.Retention
import org.migor.feedless.generated.types.StringFilterParams
import org.migor.feedless.generated.types.StringFilterParamsInput
import org.migor.feedless.group.GroupEntity
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.user.UserEntity
import org.migor.feedless.util.toMillis
import org.springframework.context.annotation.Lazy
import java.sql.Types
import java.time.LocalDateTime
import java.util.*

data class PluginExecution(val id: String, val params: PluginExecutionJsonEntity)

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
  @Size(message = "title", min = 1, max = LEN_STR_DEFAULT)
  open lateinit var title: String

  @Size(message = "description", max = 1024)
  @Column(name = StandardJpaFields.description, nullable = false, length = 1024)
  open lateinit var description: String

  @JdbcTypeCode(Types.ARRAY)
  @Column(name = "tags", columnDefinition = "text[]", nullable = false)
  open var tags: Array<String> = emptyArray()

  @Column(name = StandardJpaFields.visibility, nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  open var visibility: EntityVisibility = EntityVisibility.isPublic

  @Size(message = "sourcesSyncCron", max = LEN_STR_DEFAULT)
  @Column(nullable = false, name = "scheduler_expression")
  open lateinit var sourcesSyncCron: String

  @Column(name = "retention_max_items")
  open var retentionMaxCapacity: Int? = null

  @Column(name = "push_notifications_muted", nullable = false)
  open var pushNotificationsMuted: Boolean = true

  @Column(name = "retention_max_age_days")
  open var retentionMaxAgeDays: Int? = null

  @Enumerated(EnumType.STRING)
  @Column(name = "retention_max_age_days_field", nullable = false, length = 50)
  open var retentionMaxAgeDaysReferenceField: MaxAgeDaysDateField = MaxAgeDaysDateField.publishedAt

  @Column(name = "last_updated_at")
  open var lastUpdatedAt: LocalDateTime = LocalDateTime.now()

  @Column(name = "disabled_from")
  open var disabledFrom: LocalDateTime? = null

  @Size(message = "shareKey", max = 10)
  @Column(name = "share_key", nullable = false, length = 10)
  open var shareKey: String = ""

  @Column(name = "sunset_after")
  open var sunsetAfterTimestamp: LocalDateTime? = null

  @Column(name = "sunset_after_total_document_count")
  open var sunsetAfterTotalDocumentCount: Int? = null

  @Column(name = "document_count_since_creation", nullable = false)
  open var documentCountSinceCreation: Int = 0

  @Column(nullable = false, name = "is_archived")
  open var archived: Boolean = false

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "for_product", length = 20)
  open lateinit var product: Vertical

  @Column(name = "trigger_scheduled_next_at")
  open var triggerScheduledNextAt: LocalDateTime? = null

  @Column(nullable = false, name = "schema_version")
  open var schemaVersion: Int = 0

  @Column(nullable = false, name = "pulls_per_month")
  open var pullsPerMonth: Int = 0

  @Column(name = "last_pull_sync")
  open var lastPullSync: LocalDateTime? = null

  @JdbcTypeCode(SqlTypes.JSON)
  @Lazy
  @Column(nullable = false, name = "plugins")
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

  @Column(name = StandardJpaFields.groupId)
  open var groupId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
    name = StandardJpaFields.groupId,
    referencedColumnName = StandardJpaFields.id,
    insertable = false,
    updatable = false,
    nullable = true,
    foreignKey = ForeignKey(
      name = "fk_repository__to__group",
      foreignKeyDefinition = "FOREIGN KEY (group_id) REFERENCES t_group(id) ON DELETE SET NULL"
    )
  )
  open var group: GroupEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "repositoryId", cascade = [CascadeType.ALL])
  open var sources: MutableList<SourceEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "repositoryId")
  open var documents: MutableList<DocumentEntity> = mutableListOf()

  @PrePersist
  fun prePersist() {
    tags = extractHashTags(description).toTypedArray()

//    if (tags != null && tags?.size!! > 10) {
//      throw IllegalArgumentException("too many tags")
//    }
    title = StringUtils.abbreviate(title, LEN_STR_DEFAULT)
    sourcesSyncCron = StringUtils.abbreviate(sourcesSyncCron, LEN_STR_DEFAULT)
    description = StringUtils.abbreviate(description, 1024)

  }
}

fun extractHashTags(text: String): List<String> {
  val hashtagRegex = Regex("#\\S+")

  return hashtagRegex.findAll(text)
    .map { it.value.substring(1) }
    .toList()
    .distinct()
}


fun RepositoryEntity.toDto(currentUserIsOwner: Boolean): Repository {
  return Repository(
    id = id.toString(),
    ownerId = ownerId.toString(),
    product = product.toDto(),
    disabledFrom = disabledFrom?.toMillis(),
    plugins = plugins.map { it.toDto() },
    archived = archived,
    retention =
    Retention(
      maxCapacity = retentionMaxCapacity,
      maxAgeDays = retentionMaxAgeDays
    ),
    shareKey = if (currentUserIsOwner) {
      shareKey
    } else {
      ""
    },
    visibility = visibility.toDto(),
    createdAt = createdAt.toMillis(),
    lastUpdatedAt = lastUpdatedAt.toMillis(),

    nextUpdateAt = triggerScheduledNextAt?.toMillis(),
    description = description,
    title = title,
    refreshCron = sourcesSyncCron,
    documentCount = 0,
    tags = tags.asList(),
    harvests = emptyList(),
    hasDisabledSources = false,
    currentUserIsOwner = currentUserIsOwner,
    pullsPerMonth = pullsPerMonth,
    annotations = null
  )
}

private fun org.migor.feedless.repository.PluginExecution.toDto(): PluginExecution {
  return PluginExecution(
    pluginId = id,
    params = params.toDto(),
  )
}

fun PluginExecutionJsonEntity.toDto(): PluginExecutionParams {
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
    summary = summary,
    inheritParams = false
  )
}

fun RecordDateField.fromDto(): MaxAgeDaysDateField {
  return when (this) {
    RecordDateField.createdAt -> MaxAgeDaysDateField.createdAt
    RecordDateField.startingAt -> MaxAgeDaysDateField.startingAt
    RecordDateField.publishedAt -> MaxAgeDaysDateField.publishedAt
  }
}

