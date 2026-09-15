package org.migor.feedless.report

import org.migor.feedless.repository.RepositoryId
import java.time.LocalDateTime

/**
 * Describes which documents a recipient should get in a report.
 *
 * Today it's derived from the [Segmentation] the user supplies at creation:
 * time window, radius, size. It's deliberately its own layer rather than the
 * Segmentation itself, because a future recommendation profile - learned
 * preferences, categories, exclusions - hooks in here. Document selection
 * then depends on this description, not on the persisted form it came from.
 */
data class SegmentSpec(
  val repositoryId: RepositoryId,
  val from: LocalDateTime,
  val until: LocalDateTime,
  val maxSize: Int,
  val near: NearFilter? = null,
  val tags: List<String> = emptyList(),
)

data class NearFilter(
  val lat: Double,
  val lng: Double,
  val distanceKm: Double,
)

/**
 * The window runs from now to one interval into the future: a report
 * announces what's coming, rather than reporting what happened.
 */
fun Segmentation.toSpec(now: LocalDateTime): SegmentSpec = SegmentSpec(
  repositoryId = repositoryId,
  from = now,
  until = now.plus(1, timeInterval),
  maxSize = size,
  near = contentSegmentLatLon?.let { point ->
    contentSegmentLatLonDistance?.let { distance ->
      NearFilter(lat = point.latitude, lng = point.longitude, distanceKm = distance)
    }
  },
)
