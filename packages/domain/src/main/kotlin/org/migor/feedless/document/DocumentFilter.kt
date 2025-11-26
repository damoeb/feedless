package org.migor.feedless.document

import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class RecordsFilter(
  val id: StringFilter? = null,
  val repository: RepositoryId,
  val source: SourceUniqueWhere? = null,
  val startedAt: DatesWhereInput? = null,
  val createdAt: DatesWhereInput? = null,
  val publishedAt: DatesWhereInput? = null,
  val updatedAt: DatesWhereInput? = null,
  val latLng: GeoPointWhereInput? = null,
  val tags: StringFilter? = null,
)

data class SourceUniqueWhere(
  val id: SourceId,
)

data class StringFilter(
  val eq: String? = null,
  val `in`: List<String>? = null,
)

data class DatesWhereInput(
  val before: LocalDateTime? = null,
  val after: LocalDateTime? = null,
  val inFuture: Boolean? = null,
)

data class GeoPointWhereInput(
  val near: GeoPointWhereNearInput? = null,
  val within: GeoPointWhereWithinInput? = null,
)

data class GeoPointWhereNearInput(
  val point: GeoPointInput,
  val distanceKm: Double,
)

data class GeoPointWhereWithinInput(
  val nw: GeoPointInput,
  val se: GeoPointInput,
)

data class GeoPointInput(
  val lat: Double,
  val lng: Double,
)

