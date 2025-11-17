package org.migor.feedless.api

import org.migor.feedless.actions.ClickPositionAction
import org.migor.feedless.actions.ClickXpathAction
import org.migor.feedless.actions.DomAction
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.actions.ExecuteAction
import org.migor.feedless.actions.ExtractBoundingBoxAction
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.HeaderAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.source.PuppeteerWaitUntil
import org.migor.feedless.source.Source
import org.migor.feedless.util.toMillis
import org.migor.feedless.generated.types.BoundingBox as BoundingBoxDto
import org.migor.feedless.generated.types.DOMActionSelect as DOMActionSelectDto
import org.migor.feedless.generated.types.DOMActionType as DOMActionTypeDto
import org.migor.feedless.generated.types.DOMElement as DOMElementDto
import org.migor.feedless.generated.types.DOMElementByNameOrXPath as DOMElementByNameOrXPathDto
import org.migor.feedless.generated.types.DOMElementByXPath as DOMElementByXPathDto
import org.migor.feedless.generated.types.DOMExtract as DOMExtractDto
import org.migor.feedless.generated.types.GeoPoint as GeoPointDto
import org.migor.feedless.generated.types.HttpFetch as HttpFetchDto
import org.migor.feedless.generated.types.HttpGetRequest as HttpGetRequestDto
import org.migor.feedless.generated.types.PluginExecution as PluginExecutionDto
import org.migor.feedless.generated.types.PuppeteerWaitUntil as PuppeteerWaitUntilDto
import org.migor.feedless.generated.types.RequestHeader as RequestHeaderDto
import org.migor.feedless.generated.types.ScrapeAction as ScrapeActionDto
import org.migor.feedless.generated.types.ScrapeBoundingBox as ScrapeBoundingBoxDto
import org.migor.feedless.generated.types.ScrapeExtract as ScrapeExtractDto
import org.migor.feedless.generated.types.ScrapeFlow as ScrapeFlowDto
import org.migor.feedless.generated.types.Source as SourceDto
import org.migor.feedless.generated.types.StringLiteralOrVariable as StringLiteralOrVariableDto
import org.migor.feedless.generated.types.ViewPort as ViewPortDto
import org.migor.feedless.generated.types.XYPosition as XYPositionDto


fun Source.toDto(): SourceDto {
  return SourceDto(
    id = id.toString(),
    disabled = disabled,
    lastErrorMessage = lastErrorMessage,
    tags = tags?.asList() ?: emptyList(),
    latLng = latLon?.let {
      GeoPointDto(
        lat = it.x,
        lng = it.y,
      )
    },
    title = title,
    recordCount = 0,
    lastRecordsRetrieved = lastRecordsRetrieved,
    lastRefreshedAt = lastRefreshedAt?.toMillis(),
    harvests = emptyList(),
    flow = ScrapeFlowDto(sequence = actions.sortedBy { it.pos }.map { it.toDto() })
  )
}


private fun DomAction.toTypeActionDto(): DOMActionTypeDto {
  return DOMActionTypeDto(
    typeValue = data!!,
    element = this.toXpathDto(),
  )
}

fun ScrapeAction.toDto(): ScrapeActionDto {
  return when (this) {
    is FetchAction -> ScrapeActionDto(fetch = toHttpFetchActionDto())
    is DomAction -> when (event) {
      DomEventType.select -> ScrapeActionDto(select = toSelectActionDto())
      DomEventType.type -> ScrapeActionDto(type = toTypeActionDto())
      DomEventType.purge -> ScrapeActionDto(purge = toXpathDto())
      DomEventType.click -> ScrapeActionDto(click = TODO())
    }

    is ClickPositionAction -> ScrapeActionDto(
      click = DOMElementDto(
        position = XYPositionDto(
          x = x,
          y = y,
        )
      )
    )

    is HeaderAction -> ScrapeActionDto(header = toHeaderActionDto())
    is ClickXpathAction -> ScrapeActionDto(
      click = DOMElementDto(
        element = DOMElementByNameOrXPathDto(
          xpath = DOMElementByXPathDto(value = xpath)
        )
      )
    )

    is ExtractBoundingBoxAction -> ScrapeActionDto(
      extract = ScrapeExtractDto(
        fragmentName = fragmentName,
        imageBased = ScrapeBoundingBoxDto(
          BoundingBoxDto(
            x = x,
            y = y,
            w = w,
            h = h
          )
        )
      )
    )

    is ExtractXpathAction -> ScrapeActionDto(
      extract = ScrapeExtractDto(
        fragmentName = fragmentName,
        selectorBased = DOMExtractDto(
          fragmentName = fragmentName,
          xpath = DOMElementByXPathDto(xpath),
          uniqueBy = uniqueBy.toDto(),
          emit = emit.map { it.toDto() }
        )
      )
    )

    is ExecuteAction -> ScrapeActionDto(execute = toPluginExecuteActionDto())

    else -> throw IllegalArgumentException("action $this cannot be transformed to dto")
  }
}

private fun HeaderAction.toHeaderActionDto(): RequestHeaderDto {
  return RequestHeaderDto(name = name, value = value)
}

private fun ExecuteAction.toPluginExecuteActionDto(): PluginExecutionDto {
  return PluginExecutionDto(
    pluginId = pluginId,
    params = executorParams!!.toDto()
  )
}

private fun DomAction.toSelectActionDto(): DOMActionSelectDto {
  return DOMActionSelectDto(
    selectValue = data!!,
    element = toXpathDto(),
  )
}

private fun FetchAction.toHttpFetchActionDto(): HttpFetchDto {
  return HttpFetchDto(
    get = HttpGetRequestDto(
      url = if (isVariable) {
        StringLiteralOrVariableDto(variable = url)
      } else {
        StringLiteralOrVariableDto(literal = url)
      },
      timeout = timeout,
      viewport = ViewPortDto(
        width = viewportWidth,
        height = viewportHeight,
        isMobile = isMobile,
        isLandscape = isLandscape
      ),
      forcePrerender = forcePrerender,
      language = language,
      waitUntil = waitUntil?.toDto() ?: PuppeteerWaitUntilDto.load,
      additionalWaitSec = additionalWaitSec,
    )
  )
}

private fun PuppeteerWaitUntil.toDto(): PuppeteerWaitUntilDto {
  return when (this) {
    PuppeteerWaitUntil.load -> PuppeteerWaitUntilDto.load
    PuppeteerWaitUntil.networkidle0 -> PuppeteerWaitUntilDto.networkidle0
    PuppeteerWaitUntil.networkidle2 -> PuppeteerWaitUntilDto.networkidle2
    PuppeteerWaitUntil.domcontentloaded -> PuppeteerWaitUntilDto.domcontentloaded
  }
}

private fun DomAction.toXpathDto(): DOMElementByXPathDto {
  return DOMElementByXPathDto(
    value = xpath,
  )
}
