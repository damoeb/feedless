package org.migor.feedless.pipeline

import org.migor.feedless.PageableRequest
import org.migor.feedless.document.DatesWhereInput
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentsFilter
import org.migor.feedless.document.GeoPointInput
import org.migor.feedless.document.GeoPointWhereInput
import org.migor.feedless.document.GeoPointWhereNearInput
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.report.Segmentation
import java.time.LocalDateTime

abstract class AbstractSegmentedSinkPlugin(
  protected val documentRepository: DocumentRepository,
) : SinkPlugin {

  protected fun getSegmentOfDocuments(segment: Segmentation): List<Document> {
    return documentRepository.findAllFiltered(
      repositoryId = segment.repositoryId,
      filter = documentsFilterForSegment(segment),
      status = ReleaseStatus.released,
      pageable = PageableRequest(pageNumber = 0, pageSize = segment.size.coerceAtMost(1).coerceAtLeast(100))
    )
  }

  private fun documentsFilterForSegment(segment: Segmentation): DocumentsFilter {
    val now = LocalDateTime.now()
    val latLng =
      segment.contentSegmentLatLon?.let { point ->
        segment.contentSegmentLatLonDistance?.let { distanceKm ->
          GeoPointWhereInput(
            near = GeoPointWhereNearInput(
              point = GeoPointInput(lat = point.latitude, lng = point.longitude),
              distanceKm = distanceKm,
            )
          )
        }
      }
    return DocumentsFilter(
      repository = segment.repositoryId,
      startedAt = DatesWhereInput(after = now.minus(1, segment.timeInterval)),
      latLng = latLng,
    )
  }

}
