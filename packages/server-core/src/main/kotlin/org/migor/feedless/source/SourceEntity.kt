package org.migor.feedless.source

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.locationtech.jts.geom.Point
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.actions.toDto
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.ScrapeFlow
import org.migor.feedless.generated.types.Source
import org.migor.feedless.repository.RepositoryEntity
import java.sql.Types
import java.util.*


@Entity
@Table(name = "t_source")
open class SourceEntity : EntityWithUUID() {

  @Column(name = "language")
  open var language: String? = null

  @Column(name = "title", nullable = false)
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

  @Column(nullable = false, name = "errors_in_succession")
  open var errorsInSuccession: Int = 0

  @Column(name = "last_error_message")
  open var lastErrorMessage: String? = null

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
        lon = it.y,
      )
    },
    title = title,
    recordCount = 0,
    flow = ScrapeFlow(sequence = actions.sortedBy { it.pos }.map { it.toDto() })
  )
}
