package org.migor.feedless.data.jpa.repository

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
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
import org.migor.feedless.Vertical
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.document.DocumentEntity
import org.migor.feedless.data.jpa.document.DocumentEntity.Companion.LEN_STR_DEFAULT
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.group.GroupEntity
import org.migor.feedless.data.jpa.source.SourceEntity
import org.migor.feedless.data.jpa.source.actions.PluginExecutionJsonEntity
import org.migor.feedless.data.jpa.user.UserEntity
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
@Table(
  name = "t_repository",
  indexes = [
    Index(name = "repository_created_at_idx", columnList = StandardJpaFields.createdAt),
  ]
)
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

  @Column(name = "push_notifications_enabled", nullable = false)
  open var pushNotificationsEnabled: Boolean = true

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
  open var plugins: List<PluginExecution> = emptyList()

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
