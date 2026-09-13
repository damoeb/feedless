package org.migor.feedless.http.mapper

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import org.migor.feedless.actions.ClickPositionAction
import org.migor.feedless.actions.ClickXpathAction
import org.migor.feedless.actions.DomAction
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.actions.ExecuteAction
import org.migor.feedless.actions.ExtractBoundingBoxAction
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.HeaderAction
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.actions.WaitAction
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.http.api.model.DomActionSelect
import org.migor.feedless.http.api.model.DomActionType
import org.migor.feedless.http.api.model.DomElement
import org.migor.feedless.http.api.model.DomElementByXPath
import org.migor.feedless.http.api.model.HttpFetch
import org.migor.feedless.http.api.model.HttpGetRequest
import org.migor.feedless.http.api.model.PluginExecution
import org.migor.feedless.http.api.model.PluginExecutionParams
import org.migor.feedless.http.api.model.RequestHeader
import org.migor.feedless.http.api.model.ScrapeAction as HttpScrapeAction
import org.migor.feedless.http.api.model.ScrapeEmit
import org.migor.feedless.http.api.model.ScrapeExtract
import org.migor.feedless.http.api.model.ScrapeFlow
import org.migor.feedless.http.api.model.SourceCreate
import org.migor.feedless.http.api.model.WaitForAction
import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.PuppeteerWaitUntil
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.StoredFlowParser
import org.springframework.stereotype.Component

@Component
class HttpScrapeFlowMapper : StoredFlowParser {

  private val gson = Gson()

  // Jackson, like the HTTP layer that parsed the flow; nulls omitted so a stored flow reads like the request.
  private val storedFlowJson = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)

  fun toDomainSource(body: SourceCreate): Source =
    Source(
      id = body.id?.let { SourceId(it) } ?: SourceId(),
      title = body.title,
      tags = body.tags?.toTypedArray(),
      latLon = body.latLng?.let { LatLonPoint(it.lat.toDouble(), it.lng.toDouble()) },
      actions = toDomainActions(body.flow),
      lastErrorMessage = body.lastErrorMessage,
    )

  fun toDomainActions(flow: ScrapeFlow): List<ScrapeAction> =
    flow.sequence.mapIndexed { index, action ->
      requireExactlyOneKind(action, index)
      toDomainAction(action) ?: throw IllegalArgumentException("flow.sequence[$index] is not a known action")
    }

  /** Stores the HTTP flow itself, so a queued run is mapped exactly as it was validated. */
  fun toStoredFlow(flow: ScrapeFlow): String {
    toDomainActions(flow)
    return storedFlowJson.writeValueAsString(flow)
  }

  override fun storedFlowToDomainActions(storedFlow: String): List<ScrapeAction> =
    toDomainActions(storedFlowJson.readValue(storedFlow, ScrapeFlow::class.java))

  /** The schema can't express oneOf, and taking the first non-null kind would silently drop the rest. */
  private fun requireExactlyOneKind(action: HttpScrapeAction, index: Int) {
    val set = listOfNotNull(
      action.fetch?.let { "fetch" },
      action.extract?.let { "extract" },
      action.execute?.let { "execute" },
      action.click?.let { "click" },
      action.type?.let { "type" },
      action.waitFor?.let { "waitFor" },
      action.select?.let { "select" },
      action.header?.let { "header" },
      action.purge?.let { "purge" },
    )
    require(set.size == 1) {
      if (set.isEmpty()) {
        "flow.sequence[$index] sets no action; exactly one of fetch, extract, execute, " +
          "click, type, waitFor, select, header, purge is required"
      } else {
        "flow.sequence[$index] sets ${set.size} actions (${set.joinToString(", ")}); exactly one is allowed"
      }
    }
  }

  fun toHttpFlow(actions: List<ScrapeAction>): ScrapeFlow =
    ScrapeFlow(sequence = actions.sortedBy { it.pos }.map { toHttpAction(it) })

  private fun toHttpAction(action: ScrapeAction): HttpScrapeAction =
    when (action) {
      is FetchAction -> HttpScrapeAction(fetch = action.toHttpFetch())
      is WaitAction -> HttpScrapeAction(waitFor = WaitForAction(element = org.migor.feedless.http.api.model.DomElementByNameOrXPath(xpath = DomElementByXPath(value = action.xpath))))
      is HeaderAction -> HttpScrapeAction(header = RequestHeader(name = action.name, value = action.value))
      is DomAction -> when (action.event) {
        DomEventType.purge -> HttpScrapeAction(purge = DomElementByXPath(value = action.xpath))
        DomEventType.type -> HttpScrapeAction(type = DomActionType(element = DomElementByXPath(value = action.xpath), typeValue = action.data ?: ""))
        DomEventType.select -> HttpScrapeAction(select = DomActionSelect(element = DomElementByXPath(value = action.xpath), selectValue = action.data ?: ""))
        DomEventType.click -> HttpScrapeAction(click = DomElement(element = org.migor.feedless.http.api.model.DomElementByNameOrXPath(xpath = DomElementByXPath(value = action.xpath))))
      }
      is ClickXpathAction -> HttpScrapeAction(click = DomElement(element = org.migor.feedless.http.api.model.DomElementByNameOrXPath(xpath = DomElementByXPath(value = action.xpath))))
      is ClickPositionAction -> HttpScrapeAction(click = DomElement(position = org.migor.feedless.http.api.model.XyPosition(x = action.x, y = action.y)))
      is ExtractBoundingBoxAction -> HttpScrapeAction(
        extract = ScrapeExtract(
          fragmentName = action.fragmentName,
          imageBased = org.migor.feedless.http.api.model.ScrapeBoundingBox(
            boundingBox = org.migor.feedless.http.api.model.BoundingBox(x = action.x, y = action.y, w = action.w, h = action.h),
          ),
        ),
      )
      is ExtractXpathAction -> HttpScrapeAction(
        extract = ScrapeExtract(
          fragmentName = action.fragmentName,
          selectorBased = org.migor.feedless.http.api.model.DomExtract(
            fragmentName = action.fragmentName,
            xpath = DomElementByXPath(value = action.xpath),
            uniqueBy = action.uniqueBy.toHttp(),
            emit = action.emit.map { it.toHttp() },
          ),
        ),
      )
      is ExecuteAction -> HttpScrapeAction(execute = action.toHttpExecute())
    }

  private fun FetchAction.toHttpFetch(): HttpFetch {
    val get = HttpGetRequest(
      url = org.migor.feedless.http.api.model.StringLiteralOrVariable(
        literal = if (!isVariable) url else null,
        variable = if (isVariable) url else null,
      ),
      timeout = timeout,
      additionalWaitSec = additionalWaitSec,
      language = language,
      forcePrerender = forcePrerender,
      waitUntil = waitUntil?.toHttp(),
      viewport = if (viewportWidth != null && viewportHeight != null) {
        org.migor.feedless.http.api.model.ViewPort(
          width = viewportWidth!!,
          height = viewportHeight!!,
          isMobile = isMobile,
          isLandscape = isLandscape,
        )
      } else {
        null
      },
    )
    return HttpFetch(get = get)
  }

  private fun ExecuteAction.toHttpExecute(): PluginExecution =
    PluginExecution(
      pluginId = pluginId,
      params = PluginExecutionParams(jsonData = executorParams?.paramsJsonString),
    )

  private fun ExtractEmit.toHttp(): ScrapeEmit = when (this) {
    ExtractEmit.text -> ScrapeEmit.text
    ExtractEmit.html -> ScrapeEmit.html
    ExtractEmit.pixel -> ScrapeEmit.pixel
    ExtractEmit.date -> ScrapeEmit.date
  }

  private fun PuppeteerWaitUntil.toHttp(): org.migor.feedless.http.api.model.PuppeteerWaitUntil = when (this) {
    PuppeteerWaitUntil.load -> org.migor.feedless.http.api.model.PuppeteerWaitUntil.load
    PuppeteerWaitUntil.domcontentloaded -> org.migor.feedless.http.api.model.PuppeteerWaitUntil.domcontentloaded
    PuppeteerWaitUntil.networkidle0 -> org.migor.feedless.http.api.model.PuppeteerWaitUntil.networkidle0
    PuppeteerWaitUntil.networkidle2 -> org.migor.feedless.http.api.model.PuppeteerWaitUntil.networkidle2
  }

  private fun toDomainAction(action: HttpScrapeAction): ScrapeAction? {
    action.fetch?.let { return it.toFetchAction() }
    action.waitFor?.let { return it.toWaitAction() }
    action.header?.let { return it.toHeaderAction() }
    action.purge?.let { return toDomAction(DomEventType.purge, it.value) }
    action.type?.let { return toDomAction(DomEventType.type, it.element.value, it.typeValue) }
    action.select?.let { return toDomAction(DomEventType.select, it.element.value, it.selectValue) }
    action.execute?.let { return it.toExecuteAction() }
    action.extract?.let { return it.toExtractAction() }
    action.click?.let { return it.toClickAction() }
    return null
  }

  private fun HttpFetch.toFetchAction(): FetchAction {
    val get = get ?: throw IllegalArgumentException("fetch.get is required")
    val base = FetchAction(
      sourceId = SourceId(),
      timeout = get.timeout,
      additionalWaitSec = get.additionalWaitSec,
      language = get.language,
      forcePrerender = get.forcePrerender == true,
      url = "",
      isVariable = false,
    )
    val withUrl = get.url.literal?.let { base.copy(url = it, isVariable = false) }
      ?: get.url.variable?.let { base.copy(url = it, isVariable = true) }
      ?: base
    return get.viewport?.let {
      withUrl.copy(
        viewportWidth = it.width,
        viewportHeight = it.height,
        isMobile = it.isMobile,
        isLandscape = it.isLandscape,
      )
    }?.copy(waitUntil = get.waitUntil?.toDomain()) ?: withUrl.copy(waitUntil = get.waitUntil?.toDomain())
  }

  private fun WaitForAction.toWaitAction(): WaitAction {
    val xpath = element.xpath?.value
      ?: throw IllegalArgumentException("waitFor.element.xpath is required")
    return WaitAction(sourceId = SourceId(), xpath = xpath)
  }

  private fun RequestHeader.toHeaderAction(): HeaderAction =
    HeaderAction(sourceId = SourceId(), name = name, value = value)

  private fun PluginExecution.toExecuteAction(): ExecuteAction =
    ExecuteAction(
      sourceId = SourceId(),
      pluginId = pluginId,
      executorParams = params.toPluginExecutionJson(),
    )

  private fun PluginExecutionParams.toPluginExecutionJson(): PluginExecutionJson {
    jsonData?.let { return PluginExecutionJson(paramsJsonString = it) }
    val payload = orgFeedlessFulltext
      ?: orgFeedlessDiffRecords
      ?: orgFeedlessFeed
    return PluginExecutionJson(paramsJsonString = payload?.let { gson.toJson(it) })
  }

  private fun ScrapeExtract.toExtractAction(): ScrapeAction {
    selectorBased?.let { domExtract ->
      return ExtractXpathAction(
        sourceId = SourceId(),
        fragmentName = fragmentName,
        xpath = domExtract.xpath.value,
        uniqueBy = domExtract.uniqueBy.toDomain(),
        emit = domExtract.emit.map { it.toDomain() }.toTypedArray(),
      )
    }
    imageBased?.let { bounding ->
      val box = bounding.boundingBox
      return ExtractBoundingBoxAction(
        sourceId = SourceId(),
        fragmentName = fragmentName,
        x = box.x,
        y = box.y,
        w = box.w,
        h = box.h,
      )
    }
    throw IllegalArgumentException("extract requires selectorBased or imageBased")
  }

  private fun DomElement.toClickAction(): ScrapeAction {
    element?.xpath?.let {
      return ClickXpathAction(sourceId = SourceId(), xpath = it.value)
    }
    position?.let {
      return ClickPositionAction(sourceId = SourceId(), x = it.x, y = it.y)
    }
    throw IllegalArgumentException("click requires element or position")
  }

  private fun toDomAction(event: DomEventType, xpath: String, data: String? = null): DomAction =
    DomAction(sourceId = SourceId(), xpath = xpath, event = event, data = data)

  private fun ScrapeEmit.toDomain(): ExtractEmit = when (this) {
    ScrapeEmit.text -> ExtractEmit.text
    ScrapeEmit.html -> ExtractEmit.html
    ScrapeEmit.pixel -> ExtractEmit.pixel
    ScrapeEmit.date -> ExtractEmit.date
  }

  private fun org.migor.feedless.http.api.model.PuppeteerWaitUntil.toDomain(): PuppeteerWaitUntil = when (this) {
    org.migor.feedless.http.api.model.PuppeteerWaitUntil.load -> PuppeteerWaitUntil.load
    org.migor.feedless.http.api.model.PuppeteerWaitUntil.domcontentloaded -> PuppeteerWaitUntil.domcontentloaded
    org.migor.feedless.http.api.model.PuppeteerWaitUntil.networkidle0 -> PuppeteerWaitUntil.networkidle0
    org.migor.feedless.http.api.model.PuppeteerWaitUntil.networkidle2 -> PuppeteerWaitUntil.networkidle2
  }
}
