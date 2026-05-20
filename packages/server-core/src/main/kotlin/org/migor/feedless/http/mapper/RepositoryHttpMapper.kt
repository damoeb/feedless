package org.migor.feedless.http.mapper

import org.migor.feedless.api.fromDto
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.api.toDto
import org.migor.feedless.generated.types.BoolUpdateOperationsInput
import org.migor.feedless.generated.types.DOMElementByXPathInput
import org.migor.feedless.generated.types.DOMExtractInput
import org.migor.feedless.generated.types.HttpFetchInput
import org.migor.feedless.generated.types.HttpGetRequestInput
import org.migor.feedless.generated.types.NullableStringUpdateOperationsInput
import org.migor.feedless.generated.types.NullableUpdateFlowInput
import org.migor.feedless.generated.types.NullableUpdateGeoPointInput
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.RetentionInput
import org.migor.feedless.generated.types.ScrapeActionInput
import org.migor.feedless.generated.types.ScrapeExtractInput
import org.migor.feedless.generated.types.ScrapeFlowInput
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.SourceUniqueWhereInput
import org.migor.feedless.generated.types.SourceUpdateDataInput
import org.migor.feedless.generated.types.SourceUpdateInput
import org.migor.feedless.generated.types.SourcesUpdateInput
import org.migor.feedless.generated.types.StringLiteralOrVariableInput
import org.migor.feedless.generated.types.StringUpdateOperationsInput
import org.migor.feedless.generated.types.VisibilityUpdateOperationsInput
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.toPageableRequest
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourcesFilter
import org.migor.feedless.user.UserId
import org.migor.feedless.api.mapper.toDto as sourceToDto
import org.migor.feedless.generated.types.GeoPointInput
import org.migor.feedless.generated.types.GeoPoint as GeoPointDto
import org.migor.feedless.repository.FulltextQueryFilter as DomainFulltextQueryFilter
import org.migor.feedless.repository.RepositoriesFilter
import org.migor.feedless.repository.VerticalFilter as DomainVerticalFilter
import org.migor.feedless.repository.VisibilityFilter as DomainVisibilityFilter
import org.migor.feedless.generated.types.ScrapeEmit as ScrapeEmitDto
import org.migor.feedless.generated.types.Vertical as VerticalDto
import org.migor.feedless.generated.types.Visibility as VisibilityDto
import org.migor.feedless.http.model.DomElementByXPath
import org.migor.feedless.http.model.DomExtract
import org.migor.feedless.http.model.GeoPoint
import org.migor.feedless.http.model.HttpFetch
import org.migor.feedless.http.model.HttpGetRequest
import org.migor.feedless.http.model.Repository as HttpRepository
import org.migor.feedless.http.model.RepositoryCreate
import org.migor.feedless.http.model.RepositoryUpdate
import org.migor.feedless.http.model.Retention
import org.migor.feedless.http.model.ScrapeAction
import org.migor.feedless.http.model.ScrapeEmit
import org.migor.feedless.http.model.ScrapeExtract
import org.migor.feedless.http.model.ScrapeFlow
import org.migor.feedless.http.model.Source as HttpSource
import org.migor.feedless.http.model.SourceCreate
import org.migor.feedless.http.model.SourceUpdate
import org.migor.feedless.http.model.SourcesPatch
import org.migor.feedless.http.model.StringLiteralOrVariable
import org.migor.feedless.http.model.Vertical
import org.migor.feedless.http.model.Visibility
import org.springframework.data.domain.PageRequest

fun Repository.toHttpDto(
  currentUserId: UserId,
  sourcesCount: Int = 0,
  sourcesCountWithProblems: Int = 0,
): HttpRepository {
  val dto = toDto(ownerId == currentUserId.uuid)
  return HttpRepository(
    id = java.util.UUID.fromString(dto.id),
    title = dto.title,
    description = dto.description,
    shareKey = dto.shareKey,
    ownerId = java.util.UUID.fromString(dto.ownerId),
    product = dto.product.toHttp(),
    visibility = dto.visibility.toHttp(),
    refreshCron = dto.refreshCron,
    tags = dto.tags,
    createdAt = dto.createdAt,
    lastUpdatedAt = dto.lastUpdatedAt,
    nextUpdateAt = dto.nextUpdateAt,
    documentCount = dto.documentCount,
    archived = dto.archived,
    pullsPerMonth = dto.pullsPerMonth,
    currentUserIsOwner = dto.currentUserIsOwner,
    pushNotificationsEnabled = dto.pushNotificationsEnabled,
    sourcesCount = sourcesCount,
    sourcesCountWithProblems = sourcesCountWithProblems,
    disabledFrom = dto.disabledFrom,
  )
}

fun Source.toHttpDto(recordCount: Int? = null): HttpSource {
  val dto = sourceToDto()
  return HttpSource(
    id = java.util.UUID.fromString(dto.id),
    title = dto.title,
    flow = dto.flow.toHttp(),
    disabled = dto.disabled,
    lastRefreshedAt = dto.lastRefreshedAt,
    lastRecordsRetrieved = dto.lastRecordsRetrieved,
    latLng = dto.latLng?.toHttp(),
    tags = dto.tags,
    recordCount = recordCount ?: dto.recordCount,
    lastErrorMessage = dto.lastErrorMessage,
  )
}

fun RepositoryCreate.toGraphql(): RepositoryCreateInput {
  return RepositoryCreateInput(
    product = product.toGraphql(),
    sources = sources.map { it.toGraphql() },
    title = title,
    description = description,
    retention = retention?.toGraphql(),
    refreshCron = refreshCron,
    withShareKey = withShareKey,
    pushNotificationsMuted = pushNotificationsMuted,
    visibility = visibility?.toGraphql(),
  )
}

fun RepositoryUpdate.toGraphql(): RepositoryUpdateDataInput {
  return RepositoryUpdateDataInput(
    title = title?.let { StringUpdateOperationsInput(set = it) },
    description = description?.let { NullableStringUpdateOperationsInput(set = it) },
    refreshCron = refreshCron?.let { NullableStringUpdateOperationsInput(set = it) },
    pushNotificationsMuted = pushNotificationsMuted?.let { BoolUpdateOperationsInput(set = it) },
    visibility = visibility?.let { VisibilityUpdateOperationsInput(set = it.toGraphql()) },
    retention = retention?.toGraphqlUpdate(),
    sources = sources?.toGraphql(),
  )
}

fun SourcesPatch.toGraphql(): SourcesUpdateInput {
  return SourcesUpdateInput(
    add = add?.map { it.toGraphql() },
    update = update?.map { it.toGraphql() },
    remove = remove?.map { it.toString() },
  )
}

fun SourceCreate.toGraphql(): SourceInput {
  return SourceInput(
    id = id?.toString(),
    title = title,
    flow = flow.toGraphql(),
    draft = draft,
    latLng = latLng?.toGraphqlInput(),
    tags = tags,
    lastErrorMessage = lastErrorMessage,
  )
}

fun SourceUpdate.toGraphql(): SourceUpdateInput {
  return SourceUpdateInput(
    where = SourceUniqueWhereInput(id = id.toString()),
    data = SourceUpdateDataInput(
      title = title?.let { StringUpdateOperationsInput(set = it) },
      tags = tags?.let { org.migor.feedless.generated.types.StringArrayUpdateOperationsInput(set = it) },
      disabled = disabled?.let { BoolUpdateOperationsInput(set = it) },
      latLng = latLng?.let { NullableUpdateGeoPointInput(set = it.toGraphqlInput()) },
      flow = flow?.let { NullableUpdateFlowInput(set = it.toGraphql()) },
    ),
  )
}

fun ScrapeFlow.toGraphql(): ScrapeFlowInput {
  return ScrapeFlowInput(sequence = sequence.map { it.toGraphql() })
}

fun ScrapeAction.toGraphql(): ScrapeActionInput {
  val fetchAction = fetch
  val extractAction = extract
  return when {
    fetchAction != null -> ScrapeActionInput(fetch = fetchAction.toGraphql())
    extractAction != null -> ScrapeActionInput(extract = extractAction.toGraphql())
    else -> throw IllegalArgumentException("ScrapeAction requires fetch or extract")
  }
}

fun HttpFetch.toGraphql(): HttpFetchInput {
  return HttpFetchInput(get = get!!.toGraphql())
}

fun HttpGetRequest.toGraphql(): HttpGetRequestInput {
  return HttpGetRequestInput(
    url = url.toGraphql(),
    timeout = timeout,
    forcePrerender = forcePrerender,
    language = language,
    additionalWaitSec = additionalWaitSec,
  )
}

fun StringLiteralOrVariable.toGraphql(): StringLiteralOrVariableInput {
  return StringLiteralOrVariableInput(literal = literal, variable = variable)
}

fun ScrapeExtract.toGraphql(): ScrapeExtractInput {
  return ScrapeExtractInput(
    fragmentName = fragmentName ?: "fragment",
    selectorBased = selectorBased!!.toGraphql(),
  )
}

fun DomExtract.toGraphql(): DOMExtractInput {
  return DOMExtractInput(
    fragmentName = fragmentName ?: "fragment",
    xpath = xpath!!.toGraphql(),
    emit = emit?.map { it.toGraphql() } ?: listOf(ScrapeEmitDto.text),
    uniqueBy = uniqueBy?.toGraphql() ?: ScrapeEmitDto.text,
  )
}

fun DomElementByXPath.toGraphql(): DOMElementByXPathInput {
  return DOMElementByXPathInput(value = xpath ?: "//")
}

fun GeoPoint.toGraphqlInput(): GeoPointInput {
  return GeoPointInput(lat = lat.toDouble(), lng = lng.toDouble())
}

fun GeoPoint.toGraphql(): GeoPointDto {
  return GeoPointDto(lat = lat.toDouble(), lng = lng.toDouble())
}

fun GeoPointDto.toHttp(): GeoPoint {
  return GeoPoint(lat = lat.toFloat(), lng = lng.toFloat())
}

fun org.migor.feedless.generated.types.ScrapeFlow.toHttp(): ScrapeFlow {
  return ScrapeFlow(sequence = sequence.map { it.toHttp() })
}

fun org.migor.feedless.generated.types.ScrapeAction.toHttp(): ScrapeAction {
  return ScrapeAction(fetch = fetch?.toHttp(), extract = extract?.toHttp())
}

fun org.migor.feedless.generated.types.HttpFetch.toHttp(): HttpFetch {
  return HttpFetch(get = get?.toHttp())
}

fun org.migor.feedless.generated.types.HttpGetRequest.toHttp(): HttpGetRequest {
  return HttpGetRequest(
    url = url.toHttp(),
    timeout = timeout,
    forcePrerender = forcePrerender,
    language = language,
    additionalWaitSec = additionalWaitSec,
  )
}

fun org.migor.feedless.generated.types.StringLiteralOrVariable.toHttp(): StringLiteralOrVariable {
  return StringLiteralOrVariable(literal = literal, variable = variable)
}

fun org.migor.feedless.generated.types.ScrapeExtract.toHttp(): ScrapeExtract {
  return ScrapeExtract(fragmentName = fragmentName, selectorBased = selectorBased?.toHttp())
}

fun org.migor.feedless.generated.types.DOMExtract.toHttp(): DomExtract {
  return DomExtract(
    fragmentName = fragmentName,
    xpath = xpath?.toHttp(),
    emit = emit?.map { it.toHttp() },
    uniqueBy = uniqueBy?.toHttp(),
  )
}

fun org.migor.feedless.generated.types.DOMElementByXPath.toHttp(): DomElementByXPath {
  return DomElementByXPath(xpath = value)
}

fun ScrapeEmit.toGraphql(): ScrapeEmitDto = when (this) {
  ScrapeEmit.TEXT -> ScrapeEmitDto.text
  ScrapeEmit.HTML -> ScrapeEmitDto.html
  ScrapeEmit.PIXEL -> ScrapeEmitDto.pixel
  ScrapeEmit.DATE -> ScrapeEmitDto.date
}

fun ScrapeEmitDto.toHttp(): ScrapeEmit = when (this) {
  ScrapeEmitDto.text -> ScrapeEmit.TEXT
  ScrapeEmitDto.html -> ScrapeEmit.HTML
  ScrapeEmitDto.pixel -> ScrapeEmit.PIXEL
  ScrapeEmitDto.date -> ScrapeEmit.DATE
}

fun Vertical.toGraphql(): VerticalDto = when (this) {
  Vertical.ALL -> VerticalDto.all
  Vertical.RSS_PROXY -> VerticalDto.rssProxy
  Vertical.VISUAL_DIFF -> VerticalDto.visualDiff
  Vertical.READER -> VerticalDto.reader
  Vertical.FEEDLESS -> VerticalDto.feedless
  Vertical.UPCOMING -> VerticalDto.upcoming
  Vertical.UNTOLD_NOTES -> VerticalDto.untoldNotes
  Vertical.PAGE_CHANGE_TRACKER -> VerticalDto.pageChangeTracker
  Vertical.FEED_DUMP -> VerticalDto.feedDump
}

fun VerticalDto.toHttp(): Vertical = when (this) {
  VerticalDto.all -> Vertical.ALL
  VerticalDto.rssProxy -> Vertical.RSS_PROXY
  VerticalDto.visualDiff -> Vertical.VISUAL_DIFF
  VerticalDto.reader -> Vertical.READER
  VerticalDto.feedless -> Vertical.FEEDLESS
  VerticalDto.upcoming -> Vertical.UPCOMING
  VerticalDto.untoldNotes -> Vertical.UNTOLD_NOTES
  VerticalDto.pageChangeTracker -> Vertical.PAGE_CHANGE_TRACKER
  VerticalDto.feedDump -> Vertical.FEED_DUMP
  else -> throw IllegalArgumentException("Unsupported vertical: $this")
}

fun Visibility.toGraphql(): VisibilityDto = when (this) {
  Visibility.IS_PRIVATE -> VisibilityDto.isPrivate
  Visibility.IS_PUBLIC -> VisibilityDto.isPublic
}

fun VisibilityDto.toHttp(): Visibility = when (this) {
  VisibilityDto.isPrivate -> Visibility.IS_PRIVATE
  VisibilityDto.isPublic -> Visibility.IS_PUBLIC
}

fun Retention.toGraphql(): RetentionInput {
  return RetentionInput(maxCapacity = maxCapacity, maxAgeDays = maxAgeDays)
}

fun Retention.toGraphqlUpdate(): org.migor.feedless.generated.types.RetentionUpdateInput {
  return org.migor.feedless.generated.types.RetentionUpdateInput(
    maxCapacity = maxCapacity?.let { org.migor.feedless.generated.types.NullableIntUpdateOperationsInput(set = it) },
    maxAgeDays = maxAgeDays?.let { org.migor.feedless.generated.types.NullableIntUpdateOperationsInput(set = it) },
  )
}

fun toRepositoriesFilter(
  product: Vertical?,
  visibility: Visibility?,
  q: String?,
): RepositoriesFilter? {
  if (product == null && visibility == null && q == null) return null
  return RepositoriesFilter(
    product = product?.let { DomainVerticalFilter(eq = it.toGraphql().fromDto()) },
    visibility = visibility?.let { DomainVisibilityFilter(`in` = listOf(it.toGraphql().fromDto())) },
    text = q?.let { DomainFulltextQueryFilter(query = it) },
    tags = null,
  )
}

fun toSourcesFilter(disabled: Boolean?, like: String?): SourcesFilter? {
  if (disabled == null && like == null) return null
  return SourcesFilter(id = null, latLng = null, like = like, disabled = disabled)
}

fun toPageable(page: Int, pageSize: Int) = PageRequest.of(page, pageSize).toPageableRequest()
