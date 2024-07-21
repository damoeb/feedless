package org.migor.feedless.api

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.actions.ClickPositionActionEntity
import org.migor.feedless.actions.ClickXpathActionEntity
import org.migor.feedless.actions.DomActionEntity
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.actions.ExtractXpathActionEntity
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.actions.HeaderActionEntity
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.actions.WaitActionEntity
import org.migor.feedless.actions.fromDto
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.models.SourceEntity
import org.migor.feedless.feed.discovery.RemoteNativeFeedRef
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.*
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.JsonUtil
import org.migor.feedless.web.ExtendContext
import org.migor.feedless.web.GenericFeedRule
import org.migor.feedless.web.GenericFeedSelectors
import org.slf4j.LoggerFactory
import java.util.*

private val log = LoggerFactory.getLogger("Mapper")

fun EntityVisibility.toDto(): Visibility = when (this) {
  EntityVisibility.isPrivate -> Visibility.isPrivate
  EntityVisibility.isPublic -> Visibility.isPublic
}

fun ScrapeResponseInput.fromDto(): ScrapeResponse = ScrapeResponse(
  errorMessage = errorMessage,
  failed = failed,
  logs = logs,
  outputs = outputs.map { it.fromDto() }
)

private fun ScrapeActionResponseInput.fromDto(): ScrapeActionResponse {
  return ScrapeActionResponse(
    extract = extract?.fromDto(),
    fetch = fetch?.fromDto()
  )
}

private fun HttpFetchResponseInput.fromDto(): HttpFetchResponse {
  return HttpFetchResponse(
    data = data,
    debug = debug.fromDto()
  )
}

private fun FetchActionDebugResponseInput.fromDto(): FetchActionDebugResponse {
  return FetchActionDebugResponse(
    corrId = corrId,
    url = url,
    screenshot = screenshot,
    prerendered = prerendered,
    console = console,
    network = network.map { it.fromDto() },
    cookies = cookies,
    statusCode = statusCode,
    contentType = contentType,
    viewport = viewport.fromDto()
  )
}

private fun NetworkRequestInput.fromDto(): NetworkRequest {
  return NetworkRequest(
    url = url,
    requestHeaders = requestHeaders,
    requestPostData = requestPostData,
    responseHeaders = responseHeaders,
    responseSize = responseSize,
    responseBody = responseBody
  )
}

private fun ScrapeExtractResponseInput.fromDto(): ScrapeExtractResponse {
  return ScrapeExtractResponse(
    fragmentName = fragmentName,
    fragments = fragments.map { it.fromDto() },
  )
}

private fun ScrapeExtractFragmentInput.fromDto(): ScrapeExtractFragment {
  return ScrapeExtractFragment(
    data = data?.fromDto(),
    html = html?.fromDto(),
    text = text?.fromDto(),
    extracts = extracts?.map { it.fromDto() },
  )
}

fun MimeDataInput.fromDto(): MimeData {
  return MimeData(
    mimeType = mimeType,
    data = data
  )
}

fun TextDataInput.fromDto(): TextData {
  return TextData(data = data)
}

fun RemoteNativeFeedRef.toDto(): RemoteNativeFeed = RemoteNativeFeed(
  feedUrl = url,
  title = title,
  description = description,
  expired = false,
  publishedAt = Date().time,
)

fun GenericFeedRule.toDto(): TransientGenericFeed {
  val selectors = Selectors(
    contextXPath = contextXPath,
    dateXPath = StringUtils.trimToEmpty(dateXPath),
    extendContext = extendContext.toDto(),
    linkXPath = linkXPath,
    dateIsStartOfEvent = dateIsStartOfEvent,
    paginationXPath = paginationXPath ?: "",
  )

  return TransientGenericFeed(
    count = count,
    hash = CryptUtil.sha1(JsonUtil.gson.toJson(selectors)),
    selectors = selectors,
    score = score,
    samples = samples.map { it.toDto() }
  )
}

fun Selectors.fromDto(): GenericFeedSelectors = GenericFeedSelectors(
  linkXPath = linkXPath,
  extendContext = fromDto(extendContext),
  contextXPath = contextXPath,
  dateXPath = dateXPath,
  dateIsStartOfEvent = BooleanUtils.isTrue(dateIsStartOfEvent),
  paginationXPath = paginationXPath
)

fun ScrapeRequestInput.fromDto(): SourceEntity {
  val source = SourceEntity()
  source.actions = flow.sequence.mapNotNull {
    it.fetch?.let { toFetchAction(it) } ?: it.wait?.let { toWaitAction(it) } ?: it.header?.let { toHeaderAction(it) }
    ?: it.purge?.let { toDomAction(DomEventType.purge, it.value) } ?: it.type?.let {
      toDomAction(
        DomEventType.type,
        it.element.value,
        it.typeValue
      )
    } ?: it.select?.let { toDomAction(DomEventType.select, it.element.value, it.selectValue) }
    ?: it.execute?.let { toExecuteAction(it) } ?: it.extract?.let { toExtractAction(it) }
    ?: it.click?.let { toClickAction(it) } ?: run {
      log.error("No mapper defined for $it")
      null
    }
  }.toMutableList()
  return source
}

fun toClickAction(it: DOMElementInput): ScrapeActionEntity? {
  return it.element?.let {
    val e = ClickXpathActionEntity()
    e.xpath = it.xpath!!.value
    e
  } ?: it.position?.let {
    val e = ClickPositionActionEntity()
    e.x = it.x
    e.y = it.y
    e
  }
}

fun toExtractAction(extract: ScrapeExtractInput): ScrapeActionEntity? {
  return extract.selectorBased?.let {
    val e = ExtractXpathActionEntity()
    e.fragmentName = extract.fragmentName
    e.xpath = it.xpath.value
    e.emit = it.emit.map { it.fromDto() }.toTypedArray()
    e
  } ?: extract.imageBased?.let {
    val e = ExtractBoundingBoxActionEntity()
    e.fragmentName = extract.fragmentName
    e.x = it.boundingBox.x
    e.y = it.boundingBox.y
    e.w = it.boundingBox.w
    e.h = it.boundingBox.h
    e
  }
}

fun toExecuteAction(it: PluginExecutionInput): ExecuteActionEntity {
  val e = ExecuteActionEntity()

  e.pluginId = it.pluginId
  e.executorParams = it.params

  return e
}

fun toHeaderAction(it: RequestHeaderInput): HeaderActionEntity {
  val e = HeaderActionEntity()
  e.name = it.name!!
  e.value = it.value!!
  return e
}

fun toWaitAction(it: WaitActionInput): WaitActionEntity {
  val e = WaitActionEntity()
  e.xpath = it.element.xpath!!.value
  return e
}

fun toFetchAction(it: HttpFetchInput): FetchActionEntity {
  val e = FetchActionEntity()
  it.get.url.literal?.let {
    e.url = it
    e.isVariable = false
  } ?: it.get.url.variable?.let {
    e.url = it
    e.isVariable = false
  }
  e.timeout = it.get.timeout
  e.additionalWaitSec = it.get.additionalWaitSec
//  e.waitUntil = it.method.get.waitUntil
  e.language = it.get.language
  e.forcePrerender = BooleanUtils.isTrue(it.get.forcePrerender)
  it.get.viewport?.let {
    e.isMobile = BooleanUtils.isTrue(it.isMobile)
    e.isLandscape = BooleanUtils.isTrue(it.isLandscape)
    e.viewportHeight = it.height
    e.viewportWidth = it.width
  }

  return e
}

fun toDomAction(domEvent: DomEventType, xpath: String, data: String? = null): DomActionEntity {
  val e = DomActionEntity()
  e.xpath = xpath
  e.event = domEvent
  e.data = data

  return e
}

private fun fromDto(extendContext: ExtendContentOptions?): ExtendContext = when (extendContext) {
  ExtendContentOptions.NEXT -> ExtendContext.NEXT
  ExtendContentOptions.PREVIOUS -> ExtendContext.PREVIOUS
  else -> ExtendContext.NONE
}

private fun PluginExecutionInput.fromDto(): PluginExecution = PluginExecution(
  pluginId = pluginId,
  params = params.fromDto(),
)

private fun PluginExecutionParamsInput.fromDto(): PluginExecutionParams = PluginExecutionParams(
  org_feedless_feed = org_feedless_feed?.fromDto(),
  jsonData = jsonData,
  org_feedless_diff_email_forward = org_feedless_diff_email_forward?.fromDto(),
  org_feedless_fulltext = org_feedless_fulltext?.fromDto(),
)

private fun FeedParamsInput.fromDto(): FeedParams {
  return FeedParams(
    generic = generic?.fromDto(),
  )
}

private fun FulltextPluginParamsInput.fromDto(): FulltextPluginParams = FulltextPluginParams(
  readability = readability,
  inheritParams = inheritParams
)

fun SelectorsInput.fromDto(): Selectors = Selectors(
  contextXPath = contextXPath,
  linkXPath = linkXPath,
  dateXPath = dateXPath,
  extendContext = extendContext,
  dateIsStartOfEvent = BooleanUtils.isTrue(dateIsStartOfEvent),
  paginationXPath = paginationXPath,
)

private fun ScrapeBoundingBoxInput.fromDto(): ScrapeBoundingBox = ScrapeBoundingBox(
  boundingBox = boundingBox.fromDto(),
)

private fun BoundingBoxInput.fromDto(): BoundingBox = BoundingBox(
  x = x,
  y = y,
  w = w,
  h = h,
)

private fun ScrapeFlowInput.fromDto(): ScrapeFlow = ScrapeFlow(
  sequence = sequence.map { it.fromDto() },
)

private fun ScrapeActionInput.fromDto(): ScrapeAction = ScrapeAction(
  fetch = fetch?.fromDto(),
  execute = execute?.fromDto(),
  extract = extract?.fromDto(),
  type = type?.fromDto(),
  purge = purge?.fromDto(),
  select = select?.fromDto(),
  click = click?.fromDto(),
  header = header?.fromDto(),
  wait = wait?.fromDto(),
)

private fun ScrapeExtractInput.fromDto(): ScrapeExtract = ScrapeExtract(
  fragmentName = fragmentName,
  imageBased = imageBased?.fromDto(),
  selectorBased = selectorBased?.fromDto(),
)

private fun DOMExtractInput.fromDto(): DOMExtract = DOMExtract(
  max = max,
  fragmentName = fragmentName,
  xpath = xpath.fromDto(),
  emit = emit,
  extract = extract?.map { it.fromDto() },
)

private fun HttpFetchInput.fromDto(): HttpFetch = HttpFetch(
  get = get.fromDto()
)

private fun StringLiteralOrVariableInput.fromDto(): StringLiteralOrVariable = StringLiteralOrVariable(
  literal = literal,
  variable = variable,
)

private fun WaitActionInput.fromDto(): WaitAction = WaitAction(
  element = element.fromDto(),
)

private fun DOMElementInput.fromDto(): DOMElement = DOMElement(
  element = element?.fromDto(),
  position = position?.fromDto(),
)

private fun XYPositionInput.fromDto(): XYPosition = XYPosition(
  x = x,
  y = y,
)

private fun RequestHeaderInput.fromDto(): RequestHeader = RequestHeader(
  name = name,
  value = value,
)

private fun DOMElementByNameOrXPathInput.fromDto(): DOMElementByNameOrXPath = DOMElementByNameOrXPath(
  name = name?.fromDto(),
  xpath = xpath?.fromDto(),
)

private fun DOMElementByNameInput.fromDto(): DOMElementByName = DOMElementByName(
  value = value,
)

private fun DOMActionSelectInput.fromDto(): DOMActionSelect = DOMActionSelect(
  element = element.fromDto(),
  selectValue = selectValue,
)

private fun DOMActionTypeInput.fromDto(): DOMActionType = DOMActionType(
  element = element.fromDto(),
  typeValue = typeValue,
)

private fun DOMElementByXPathInput.fromDto(): DOMElementByXPath = DOMElementByXPath(
  value = value,
)

private fun ScrapePrerenderInput.fromDto(): ScrapePrerender = ScrapePrerender(
  waitUntil = waitUntil,
  language = language,
  viewport = viewport?.fromDto(),
  additionalWaitSec = additionalWaitSec,
  url = url.fromDto()
)

private fun ViewPortInput.fromDto(): ViewPort = ViewPort(
  height = height,
  width = width,
  isMobile = isMobile,
  isLandscape = isLandscape,
)

private fun ExtendContext.toDto(): ExtendContentOptions {
  return when (this) {
    ExtendContext.PREVIOUS -> ExtendContentOptions.PREVIOUS
    ExtendContext.NEXT -> ExtendContentOptions.NEXT
    ExtendContext.NONE -> ExtendContentOptions.NONE
    ExtendContext.PREVIOUS_AND_NEXT -> ExtendContentOptions.PREVIOUS_AND_NEXT
  }
}

private fun JsonItem.toDto(): WebDocument {
  var contentHtmlParam: String? = null
  var contentRawBase64Param: String? = null
  var contentRawMimeParam: String? = null
  if (isHtml(contentRawMime)) {
    try {
      contentHtmlParam = String(Base64.getDecoder().decode(contentRawBase64))
    } catch (e: Exception) {
      contentHtmlParam = contentRawBase64
    }
  } else {
    contentRawBase64Param = contentRawBase64
    contentRawMimeParam = contentRawMime
  }

  return WebDocument(
    id = UUID.randomUUID().toString(),
    url = url,
    contentHtml = contentHtmlParam,
    contentRawBase64 = contentRawBase64Param,
    contentRawMime = contentRawMimeParam,
    contentText = contentText,
    publishedAt = publishedAt.time,
    startingAt = startingAt?.time,
    localized = latLng?.let { GeoPoint(lat=it.x, lon=it.y) },
    updatedAt = publishedAt.time,
    createdAt = Date().time,
  )
}


fun isHtml(contentRawMime: String?): Boolean = contentRawMime?.lowercase()?.startsWith("text/html") == true

private fun DiffEmailForwardParamsInput.fromDto(): DiffEmailForwardParams = DiffEmailForwardParams(
  inlineLatestImage = inlineLatestImage,
  inlineDiffImage = inlineDiffImage,
  inlinePreviousImage = inlinePreviousImage,
  nextItemMinIncrement = nextItemMinIncrement,
  compareBy = compareBy.fromDto(),
)

private fun CompareByInput.fromDto(): CompareBy {
  return CompareBy(
    field = field,
    fragmentNameRef = fragmentNameRef,
  )
}
