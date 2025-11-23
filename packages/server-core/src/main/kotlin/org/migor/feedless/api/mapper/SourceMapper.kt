package org.migor.feedless.api.mapper

import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.generated.types.GeoPoint as GeoPointDto
import org.migor.feedless.generated.types.ScrapeFlow as ScrapeFlowDto
import org.migor.feedless.generated.types.Source as SourceDto


fun Source.toDto(): SourceDto {
    return SourceDto(
        id = this.id.toString(),
        disabled = this.disabled,
        lastErrorMessage = this.lastErrorMessage,
        tags = this.tags?.toList() ?: emptyList(),
        latLng = this.toLatLng(),
        title = this.title,
        recordCount = 0,
        lastRecordsRetrieved = this.lastRecordsRetrieved,
        lastRefreshedAt = MapperUtil.toMillis(this.lastRefreshedAt),
        harvests = emptyList(),
        flow = this.toFlow()
    )
}

fun Source.toLatLng(): GeoPointDto? {
    return this.latLon?.let {
        GeoPointDto(
            lat = it.latitude,
            lng = it.longitude,
        )
    }
}

fun Source.toFlow(): ScrapeFlowDto {
    return ScrapeFlowDto(
        sequence = this.actions.sortedBy { it.pos }.map { it.toDto() }
    )
}

fun SourceInput.toSource(): Source {
    return Source(
        id = SourceId(),
        title = this.title,
        tags = this.tags?.toTypedArray() ?: emptyArray(),
        latLon = this.latLng?.let { LatLonPoint(it.lat, it.lng) },
        actions = this.flow.fromDto(),
        repositoryId = RepositoryId(),
    )
}
