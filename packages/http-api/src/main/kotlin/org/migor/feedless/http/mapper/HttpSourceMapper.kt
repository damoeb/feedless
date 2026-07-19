package org.migor.feedless.http.mapper

import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.http.api.model.GeoPoint
import org.migor.feedless.http.api.model.SourceCreate
import org.migor.feedless.http.api.model.SourceUpdate
import org.migor.feedless.repository.RepositorySourceUpdate
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.util.toMillis
import org.springframework.stereotype.Component
import org.migor.feedless.http.api.model.Source as HttpSource

@Component
class HttpSourceMapper(
  private val scrapeFlowMapper: HttpScrapeFlowMapper,
) {

  fun toHttp(source: Source): HttpSource =
    HttpSource(
      id = source.id.uuid,
      title = source.title,
      flow = scrapeFlowMapper.toHttpFlow(source.actions),
      disabled = source.disabled,
      lastRefreshedAt = source.lastRefreshedAt?.toMillis(),
      lastRecordsRetrieved = source.lastRecordsRetrieved,
      latLng = source.latLon?.let { GeoPoint(lat = it.latitude.toFloat(), lng = it.longitude.toFloat()) },
      tags = source.tags?.toList(),
      recordCount = 0,
      lastErrorMessage = source.lastErrorMessage,
    )

  fun toDomainSource(body: SourceCreate): Source =
    scrapeFlowMapper.toDomainSource(body)

  fun toDomainUpdate(sourceId: java.util.UUID, update: SourceUpdate): RepositorySourceUpdate =
    RepositorySourceUpdate(
      sourceId = SourceId(sourceId.toString()),
      title = update.title,
      tags = update.tags,
      disabled = update.disabled,
      latLng = update.latLng?.let { LatLonPoint(it.lat.toDouble(), it.lng.toDouble()) },
      actions = update.flow?.let { scrapeFlowMapper.toDomainActions(it) },
      clearActions = false,
    )
}
