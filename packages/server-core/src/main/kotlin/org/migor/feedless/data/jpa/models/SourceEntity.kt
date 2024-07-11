package org.migor.feedless.data.jpa.models

import io.hypersistence.utils.hibernate.type.array.StringArrayType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type
import org.locationtech.jts.geom.Point
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.actions.toDto
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.ScrapeFlow
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.repository.RepositoryEntity
import java.util.*


@Entity
@Table(name = "t_source")
open class SourceEntity : EntityWithUUID() {

  @Column(name = "language")
  open var language: String? = null

  @Column(nullable = true, name = "lat_lon", columnDefinition = "geometry")
  open var latLon: Point? = null

  @Type(StringArrayType::class)
  @Column(name = "tags", columnDefinition = "text[]")
  open var tags: Array<String>? = emptyArray()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "sourceId", cascade = [CascadeType.ALL])
  open var actions: MutableList<ScrapeActionEntity> = mutableListOf()

  @Column(name = StandardJpaFields.repositoryId, nullable = false)
  open lateinit var repositoryId: UUID

  @Column(nullable = false, name = "erroneous")
  open var erroneous: Boolean = false

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

fun SourceEntity.toDto(corrId: String): ScrapeRequest {
  return ScrapeRequest(
    id = id.toString(),
    corrId = corrId,
    errornous = erroneous,
    lastErrorMessage = lastErrorMessage,
    tags = tags?.asList() ?: emptyList(),
    localized = latLon?.let {
      GeoPoint(
        lat = it.x,
        lon = it.y,
      )
    },
    flow = ScrapeFlow(sequence = actions.map { it.toDto() })
  )
}
