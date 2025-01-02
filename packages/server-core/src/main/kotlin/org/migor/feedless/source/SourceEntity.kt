package org.migor.feedless.source

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
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
import org.locationtech.jts.geom.Point
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.actions.toDto
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.document.DocumentEntity.Companion.LEN_STR_DEFAULT
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.ScrapeFlow
import org.migor.feedless.generated.types.Source
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.util.toMillis
import java.sql.Types
import java.time.LocalDateTime
import java.util.*


@Entity
@Table(name = "t_source",
  indexes = [
    Index(name = "source_created_at_idx", columnList = StandardJpaFields.createdAt),
  ]
)
open class SourceEntity : EntityWithUUID() {

  @Column(name = "language")
  open var language: String? = null

  @Column(name = "title", nullable = false)
  @Size(message = "title", min = 0, max = LEN_STR_DEFAULT)
  open lateinit var title: String

  @Column(nullable = true, name = "lat_lon", columnDefinition = "geometry")
  open var latLon: Point? = null

  @JdbcTypeCode(Types.ARRAY)
  @Column(name = "tags", columnDefinition = "text[]")
  open var tags: Array<String>? = emptyArray()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "sourceId", cascade = [CascadeType.ALL])
  open var actions: MutableList<ScrapeActionEntity> = mutableListOf()

  @Column(name = StandardJpaFields.repositoryId, nullable = false)
  open lateinit var repositoryId: UUID

  @Column(nullable = false, name = "is_disabled")
  open var disabled: Boolean = false

  @Column(nullable = false, name = "last_records_retrieved")
  open var lastRecordsRetrieved: Int = 0

  @Column(name = "last_refreshed_at")
  open var lastRefreshedAt: LocalDateTime? = null

  @Column(nullable = false, name = "errors_in_succession")
  open var errorsInSuccession: Int = 0

  @Column(name = "last_error_message")
  @Size(message = "lastErrorMessage", min = 0, max = LEN_STR_DEFAULT)
  open var lastErrorMessage: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, LEN_STR_DEFAULT)
    }

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.repositoryId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_source__to__repository")
  )
  open var repository: RepositoryEntity? = null

  @PrePersist
  fun prePersist() {
    if (tags != null && tags?.size!! > 10) {
      throw IllegalArgumentException("too many tags")
    }
    title = StringUtils.defaultIfBlank(StringUtils.abbreviate(title, LEN_STR_DEFAULT), "None")
    lastErrorMessage = StringUtils.abbreviate(lastErrorMessage, LEN_STR_DEFAULT)
  }
}

fun SourceEntity.toDto(): Source {
  return Source(
    id = id.toString(),
    disabled = disabled,
    lastErrorMessage = lastErrorMessage,
    tags = tags?.asList() ?: emptyList(),
    latLng = latLon?.let {
      GeoPoint(
        lat = it.x,
        lng = it.y,
      )
    },
    title = title,
    recordCount = 0,
    lastRecordsRetrieved = lastRecordsRetrieved,
    lastRefreshedAt = lastRefreshedAt?.toMillis(),
    flow = ScrapeFlow(sequence = actions.sortedBy { it.pos }.map { it.toDto() })
  )
}
