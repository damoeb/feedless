package org.migor.feedless.http.mapper

import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.http.api.model.GeoPoint
import org.migor.feedless.http.api.model.SourceCreate
import org.migor.feedless.http.api.model.SourceUpdate
import org.migor.feedless.repository.RepositorySourceUpdate
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.util.toOffsetDateTime
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
      lastRefreshedAt = source.lastRefreshedAt?.toOffsetDateTime(),
      lastRecordsRetrieved = source.lastRecordsRetrieved,
      latLng = source.latLon?.let { GeoPoint(lat = it.latitude, lng = it.longitude) },
      tags = source.tags?.toList(),
      // Not computed on this path — omit rather than report a hardcoded 0.
      recordCount = null,
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
      latLng = update.latLng?.let { LatLonPoint(it.lat, it.lng) },
      actions = update.flow?.let { scrapeFlowMapper.toDomainActions(it) },
      clearActions = false,
    )
}
