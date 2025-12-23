package org.migor.feedless.api.mapper

import com.google.gson.Gson
import org.apache.commons.lang3.BooleanUtils
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
import org.migor.feedless.actions.WaitAction
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.toDto
import org.migor.feedless.generated.types.DOMElementInput
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.HttpFetchInput
import org.migor.feedless.generated.types.PluginExecutionInput
import org.migor.feedless.generated.types.RequestHeaderInput
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapeExtractInput
import org.migor.feedless.generated.types.ScrapeFlowInput
import org.migor.feedless.generated.types.WaitForActionInput
import org.migor.feedless.pipeline.plugins.CompareBy
import org.migor.feedless.pipeline.plugins.ConditionalTag
import org.migor.feedless.pipeline.plugins.DiffRecordsParams
import org.migor.feedless.pipeline.plugins.FeedPluginParams
import org.migor.feedless.pipeline.plugins.FulltextPluginParams
import org.migor.feedless.pipeline.plugins.ItemFilterParams
import org.migor.feedless.repository.toParams
import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.SourceId
import org.slf4j.LoggerFactory
import org.migor.feedless.generated.types.BoundingBox as BoundingBoxDto
import org.migor.feedless.generated.types.DOMActionSelect as DOMActionSelectDto
import org.migor.feedless.generated.types.DOMActionType as DOMActionTypeDto
import org.migor.feedless.generated.types.DOMElement as DOMElementDto
import org.migor.feedless.generated.types.DOMElementByNameOrXPath as DOMElementByNameOrXPathDto
import org.migor.feedless.generated.types.DOMElementByXPath as DOMElementByXPathDto
import org.migor.feedless.generated.types.DOMExtract as DOMExtractDto
import org.migor.feedless.generated.types.HttpFetch as HttpFetchDto
import org.migor.feedless.generated.types.HttpGetRequest as HttpGetRequestDto
import org.migor.feedless.generated.types.PluginExecution as PluginExecutionDto
import org.migor.feedless.generated.types.PluginExecutionParams as PluginExecutionParamsDto
import org.migor.feedless.generated.types.PuppeteerWaitUntil as PuppeteerWaitUntilDto
import org.migor.feedless.generated.types.RequestHeader as RequestHeaderDto
import org.migor.feedless.generated.types.ScrapeAction as ScrapeActionDto
import org.migor.feedless.generated.types.ScrapeBoundingBox as ScrapeBoundingBoxDto
import org.migor.feedless.generated.types.ScrapeExtract as ScrapeExtractDto
import org.migor.feedless.generated.types.StringLiteralOrVariable as StringLiteralOrVariableDto
import org.migor.feedless.generated.types.ViewPort as ViewPortDto
import org.migor.feedless.generated.types.XYPosition as XYPositionDto

private val log = LoggerFactory.getLogger("ScrapeActionMapper")

// Domain to DTO conversion
fun ScrapeAction.toDto(): ScrapeActionDto {
  return when (this) {
    is FetchAction -> ScrapeActionDto(fetch = this.toHttpFetchDto())
    is DomAction -> when (this.event) {
      DomEventType.select -> ScrapeActionDto(select = this.toDomActionSelectDto())
      DomEventType.type -> ScrapeActionDto(type = this.toDomActionTypeDto())
      DomEventType.purge -> ScrapeActionDto(purge = this.toDomElementByXPathDto())
      DomEventType.click -> ScrapeActionDto(
        click = DOMElementDto(
          element = DOMElementByNameOrXPathDto(
            xpath = DOMElementByXPathDto(
              value = this.xpath
            )
          )
        )
      )
    }

    is ClickPositionAction -> ScrapeActionDto(
      click = DOMElementDto(
        position = XYPositionDto(x = this.x, y = this.y)
      )
    )

    is HeaderAction -> ScrapeActionDto(header = RequestHeaderDto(name = this.name, value = this.value))
    is ClickXpathAction -> ScrapeActionDto(
      click = DOMElementDto(
        element = DOMElementByNameOrXPathDto(
          xpath = DOMElementByXPathDto(value = this.xpath)
        )
      )
    )

    is ExtractBoundingBoxAction -> ScrapeActionDto(
      extract = ScrapeExtractDto(
        fragmentName = this.fragmentName,
        imageBased = ScrapeBoundingBoxDto(
          BoundingBoxDto(x = this.x, y = this.y, w = this.w, h = this.h)
        )
      )
    )

    is ExtractXpathAction -> ScrapeActionDto(
      extract = ScrapeExtractDto(
        fragmentName = this.fragmentName,
        selectorBased = DOMExtractDto(
          fragmentName = this.fragmentName,
          xpath = DOMElementByXPathDto(this.xpath),
          uniqueBy = this.uniqueBy.toDto(),
          emit = this.emit.map { it.toDto() }
        )
      )
    )

    is ExecuteAction -> ScrapeActionDto(execute = this.toPluginExecutionDto())
    else -> throw IllegalArgumentException("action $this cannot be transformed to dto")
  }
}

private fun ExtractEmit.toDto(): ScrapeEmit {
  return when (this) {
    ExtractEmit.text -> ScrapeEmit.text
    ExtractEmit.html -> ScrapeEmit.html
    ExtractEmit.pixel -> ScrapeEmit.pixel
    ExtractEmit.date -> ScrapeEmit.date
  }
}

private fun FetchAction.toHttpFetchDto(): HttpFetchDto {
  return HttpFetchDto(
    get = HttpGetRequestDto(
      url = if (this.isVariable) {
        StringLiteralOrVariableDto(variable = this.url)
      } else {
        StringLiteralOrVariableDto(literal = this.url)
      },
      timeout = this.timeout,
      viewport = ViewPortDto(
        width = this.viewportWidth ?: 1024,
        height = this.viewportHeight ?: 768,
        isMobile = this.isMobile,
        isLandscape = this.isLandscape
      ),
      forcePrerender = this.forcePrerender,
      language = this.language,
      waitUntil = this.waitUntil?.toDto() ?: PuppeteerWaitUntilDto.load,
      additionalWaitSec = this.additionalWaitSec,
    )
  )
}

private fun org.migor.feedless.source.PuppeteerWaitUntil.toDto(): PuppeteerWaitUntilDto {
  return when (this) {
    org.migor.feedless.source.PuppeteerWaitUntil.load -> PuppeteerWaitUntilDto.load
    org.migor.feedless.source.PuppeteerWaitUntil.networkidle0 -> PuppeteerWaitUntilDto.networkidle0
    org.migor.feedless.source.PuppeteerWaitUntil.networkidle2 -> PuppeteerWaitUntilDto.networkidle2
    org.migor.feedless.source.PuppeteerWaitUntil.domcontentloaded -> PuppeteerWaitUntilDto.domcontentloaded
  }
}

private fun DomAction.toDomActionSelectDto(): DOMActionSelectDto {
  return DOMActionSelectDto(
    selectValue = this.data!!,
    element = this.toDomElementByXPathDto(),
  )
}

private fun DomAction.toDomActionTypeDto(): DOMActionTypeDto {
  return DOMActionTypeDto(
    typeValue = this.data!!,
    element = this.toDomElementByXPathDto(),
  )
}

private fun DomAction.toDomElementByXPathDto(): DOMElementByXPathDto {
  return DOMElementByXPathDto(value = this.xpath)
}

private fun ExecuteAction.toPluginExecutionDto(): PluginExecutionDto {
  return PluginExecutionDto(
    pluginId = this.pluginId,
    params = this.executorParams?.toDto(this.pluginId) ?: PluginExecutionParamsDto()
  )
}

private fun org.migor.feedless.actions.PluginExecutionJson.toDto(pluginId: String): PluginExecutionParamsDto {
  return if (this.paramsJsonString != null) {
    when (pluginId) {
      FeedlessPlugins.org_feedless_fulltext.name -> PluginExecutionParamsDto(
        org_feedless_fulltext = Gson().fromJson(
          this.paramsJsonString,
          FulltextPluginParams::class.java
        ).toDto()
      )

      FeedlessPlugins.org_feedless_diff_records.name -> PluginExecutionParamsDto(
        org_feedless_diff_records = Gson().fromJson(
          this.paramsJsonString,
          DiffRecordsParams::class.java
        ).toDto()
      )

      FeedlessPlugins.org_feedless_filter.name -> PluginExecutionParamsDto(
        org_feedless_filter = Gson().fromJson(
          this.paramsJsonString,
          Array<ItemFilterParams>::class.java
        ).toList().map { it.toDto() }
      )

      FeedlessPlugins.org_feedless_conditional_tag.name -> PluginExecutionParamsDto(
        org_feedless_conditional_tag = Gson().fromJson(
          this.paramsJsonString,
          Array<ConditionalTag>::class.java
        ).toList().map { it.toDto() }
      )

      FeedlessPlugins.org_feedless_feed.name -> PluginExecutionParamsDto(
        org_feedless_feed = Gson().fromJson(
          this.paramsJsonString,
          FeedPluginParams::class.java
        ).toDto()
      )

      else -> {
        log.warn("Unsupported plugin type for conversion to DTO: $pluginId, using jsonDataFar9f")
        PluginExecutionParamsDto(jsonData = this.paramsJsonString)
      }
    }
  } else {
    PluginExecutionParamsDto()
  }
}

fun FulltextPluginParams.toDto(): org.migor.feedless.generated.types.FulltextPluginParams {
  return org.migor.feedless.generated.types.FulltextPluginParams(
    readability = this.readability,
    summary = this.summary,
    inheritParams = this.inheritParams
  )
}

fun FeedPluginParams.toDto(): org.migor.feedless.generated.types.FeedParams {
  return org.migor.feedless.generated.types.FeedParams(
    generic = generic?.let {
      org.migor.feedless.generated.types.Selectors(
        contextXPath = it.contextXPath,
        dateIsStartOfEvent = it.dateIsStartOfEvent,
        dateXPath = it.dateXPath ?: "",
        extendContext = it.extendContext.toDto(),
        paginationXPath = it.paginationXPath ?: "",
        linkXPath = it.linkXPath,
      )
    }
  )
}

fun DiffRecordsParams.toDto(): org.migor.feedless.generated.types.DiffRecordsParams {
  return org.migor.feedless.generated.types.DiffRecordsParams(
    compareBy = this.compareBy.toDto(),
    nextItemMinIncrement = this.nextItemMinIncrement,
    inlineDiffImage = this.inlineDiffImage,
    inlineLatestImage = this.inlineLatestImage,
    inlinePreviousImage = this.inlinePreviousImage
  )
}

fun CompareBy.toDto(): org.migor.feedless.generated.types.CompareBy {
  return org.migor.feedless.generated.types.CompareBy(
    field = field.toDto(),
    fragmentNameRef = fragmentNameRef,
  )
}

fun ItemFilterParams.toDto(): org.migor.feedless.generated.types.ItemFilterParams {
  return org.migor.feedless.generated.types.ItemFilterParams(
    composite = this.composite?.toDto(),
    expression = this.expression
  )
}

fun org.migor.feedless.pipeline.plugins.CompositeFilterParams.toDto(): org.migor.feedless.generated.types.CompositeFilterParams {
  return org.migor.feedless.generated.types.CompositeFilterParams(
    exclude = this.exclude?.toDto(),
    include = this.include?.toDto()
  )
}

fun org.migor.feedless.pipeline.plugins.CompositeFieldFilterParams.toDto(): org.migor.feedless.generated.types.CompositeFieldFilterParams {
  return org.migor.feedless.generated.types.CompositeFieldFilterParams(
    index = this.index?.toDto(),
    title = this.title?.toDto(),
    content = this.content?.toDto(),
    link = this.link?.toDto()
  )
}

fun org.migor.feedless.pipeline.plugins.NumericalFilterParams.toDto(): org.migor.feedless.generated.types.NumericalFilterParams {
  return org.migor.feedless.generated.types.NumericalFilterParams(
    operator = this.operator.toDto(),
    value = this.value
  )
}

fun org.migor.feedless.pipeline.plugins.NumberFilterOperator.toDto(): org.migor.feedless.generated.types.NumberFilterOperator {
  return when (this) {
    org.migor.feedless.pipeline.plugins.NumberFilterOperator.gt -> org.migor.feedless.generated.types.NumberFilterOperator.gt
    org.migor.feedless.pipeline.plugins.NumberFilterOperator.eq -> org.migor.feedless.generated.types.NumberFilterOperator.eq
    org.migor.feedless.pipeline.plugins.NumberFilterOperator.lt -> org.migor.feedless.generated.types.NumberFilterOperator.lt
  }
}

fun org.migor.feedless.pipeline.plugins.StringFilterParams.toDto(): org.migor.feedless.generated.types.StringFilterParams {
  return org.migor.feedless.generated.types.StringFilterParams(
    operator = this.operator.toDto(),
    value = this.value
  )
}

private fun ConditionalTag.toDto(): org.migor.feedless.generated.types.ConditionalTag {
  return org.migor.feedless.generated.types.ConditionalTag(
    tag = this.tag,
    filter = this.filter.toDto()
  )
}

// DTO to Domain conversion
fun ScrapeFlowInput.fromDto(): MutableList<ScrapeAction> {
  return this.sequence.mapNotNull { actionDto ->


    actionDto.fetch?.let { it.toAction() }
      ?: actionDto.waitFor?.let { it.toAction() }
      ?: actionDto.header?.let { it.toAction() }
      ?: actionDto.purge?.let { toDomAction(DomEventType.purge, it.value) }
      ?: actionDto.type?.let { toDomAction(DomEventType.type, it.element.value, it.typeValue) }
      ?: actionDto.select?.let { toDomAction(DomEventType.select, it.element.value, it.selectValue) }
      ?: actionDto.execute?.let { it.toAction() }
      ?: actionDto.extract?.let { it.toAction() }
      ?: actionDto.click?.let { it.toAction() }
      ?: run {
        log.error("ScrapeActionMapper.fromDto() failed: No mapper defined for $actionDto")
        null
      }
  }.toMutableList()
}

private fun DOMElementInput.toAction(): ScrapeAction? {
  return this.element?.let {
    ClickXpathAction(
      sourceId = SourceId(),
      xpath = it.xpath!!.value
    )
  } ?: this.position?.let {
    ClickPositionAction(
      sourceId = SourceId(),
      x = it.x,
      y = it.y
    )
  }
}

private fun ScrapeExtractInput.toAction(): ScrapeAction? {
  return this.selectorBased?.let {
    ExtractXpathAction(
      sourceId = SourceId(),
      fragmentName = this.fragmentName,
      xpath = it.xpath.value,
      uniqueBy = it.uniqueBy.fromDto(),
      emit = this.selectorBased!!.emit.map { it.fromDto() }.toTypedArray()
    )
  } ?: this.imageBased?.let {
    ExtractBoundingBoxAction(
      sourceId = SourceId(),
      fragmentName = this.fragmentName,
      x = it.boundingBox.x,
      y = it.boundingBox.y,
      w = it.boundingBox.w,
      h = it.boundingBox.h
    )
  }
}

private fun PluginExecutionInput.toAction(): ExecuteAction {
  return ExecuteAction(
    sourceId = SourceId(),
    pluginId = this.pluginId,
    executorParams = this.params.toParams()
  )
}

private fun RequestHeaderInput.toAction(): HeaderAction {
  return HeaderAction(
    sourceId = SourceId(),
    name = this.name!!,
    value = this.value!!
  )
}

private fun WaitForActionInput.toAction(): WaitAction {
  return WaitAction(
    sourceId = SourceId(),
    xpath = this.element.xpath!!.value
  )
}

private fun HttpFetchInput.toAction(): FetchAction {
  val action = FetchAction(
    sourceId = SourceId(),
    timeout = this.get.timeout,
    additionalWaitSec = this.get.additionalWaitSec,
    language = this.get.language,
    forcePrerender = BooleanUtils.isTrue(this.get.forcePrerender),
    url = "",
    isVariable = false,
  )

  val withUrl = this.get.url.literal?.let {
    action.copy(
      url = it,
      isVariable = false
    )
  } ?: this.get.url.variable?.let {
    action.copy(
      url = it,
      isVariable = false
    )
  } ?: action

  val withViewport = this.get.viewport?.let {
    withUrl.copy(
      isMobile = BooleanUtils.isTrue(it.isMobile),
      isLandscape = BooleanUtils.isTrue(it.isLandscape),
      viewportHeight = it.height,
      viewportWidth = it.width
    )
  } ?: withUrl

  return withViewport
}

private fun toDomAction(domEvent: DomEventType, xpath: String, data: String? = null): DomAction {
  return DomAction(
    sourceId = SourceId(),
    xpath = xpath,
    event = domEvent,
    data = data
  )
}
