package org.migor.feedless.util

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.graphql.DtoResolver.toDTO
import org.migor.feedless.generated.types.*
import org.migor.feedless.web.ExtendContext
import org.migor.feedless.web.GenericFeedParserOptions
import org.migor.feedless.web.GenericFeedRefineOptions
import org.migor.feedless.web.GenericFeedRule
import org.migor.feedless.web.GenericFeedSelectors
import org.migor.feedless.web.GenericFeedSpecification
import org.migor.feedless.web.PuppeteerWaitUntil
import java.util.*
import org.migor.feedless.generated.types.PuppeteerWaitUntil as PuppeteerWaitUntilDto

object GenericFeedUtil {
  private fun fromDto(extendContext: ExtendContentOptions?): ExtendContext {
    return when (extendContext) {
      ExtendContentOptions.NEXT -> ExtendContext.NEXT
      ExtendContentOptions.PREVIOUS -> ExtendContext.PREVIOUS
      ExtendContentOptions.NONE -> ExtendContext.NONE
      else -> throw RuntimeException("ExtendContentOptionsDto $extendContext is not supported")
    }
  }

  private fun fromDto(selectors: SelectorsInput): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = selectors.linkXPath,
      extendContext = fromDto(selectors.extendContext),
      contextXPath = selectors.contextXPath,
      dateXPath = selectors.dateXPath,
      dateIsStartOfEvent = BooleanUtils.isTrue(selectors.dateIsStartOfEvent)
    )
  }

  fun fromDto(selectors: Selectors): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = selectors.linkXPath,
      extendContext = fromDto(selectors.extendContext),
      contextXPath = selectors.contextXPath,
      dateXPath = selectors.dateXPath,
      dateIsStartOfEvent = BooleanUtils.isTrue(selectors.dateIsStartOfEvent)
    )
  }

  fun fromDto(specification: GenericFeedSpecificationInput): GenericFeedSpecification {
    return GenericFeedSpecification(
      selectors = fromDto(specification.selectors),
      parserOptions = GenericFeedParserOptions(),
      scrapeOptions = fromDto(specification.scrapeOptions),
      refineOptions = fromDto(specification.refineOptions)
    )
  }

  fun fromDto(refineOptions: RefineOptions): GenericFeedRefineOptions {
    return GenericFeedRefineOptions(
      filter = StringUtils.trimToNull(refineOptions.filter),
    )
  }

  private fun fromDto(refineOptions: RefineOptionsInput): GenericFeedRefineOptions {
    return GenericFeedRefineOptions(
      filter = StringUtils.trimToNull(refineOptions.filter),
    )
  }


  fun fromDto(it: ScrapeRequestInput): ScrapeRequest {
    return ScrapeRequest.newBuilder()
      .id("")
      .corrId("")
      .emit(it.emit?.map { fromDto(it) })
      .page(fromDto(it.page))
      .debug(fromDto(it.debug))
      .build()
  }

  private fun fromDto(it: ScrapeEmitInput): ScrapeEmit {
    return ScrapeEmit.newBuilder()
      .imageBased(fromDto(it.imageBased))
      .selectorBased(fromDto(it.selectorBased))
      .build()
  }

  private fun fromDto(selectorBased: ScrapeSelectorInput?): ScrapeSelector? {
    return selectorBased?.let {
      ScrapeSelector.newBuilder()
        .xpath(fromDto(it.xpath))
        .expose(fromDto(it.expose))
        .max(it.max)
        .min(it.min)
        .build()
    }
  }

  private fun fromDto(expose: ScrapeSelectorExposeInput?): ScrapeSelectorExpose? {
    return expose?.let {
      ScrapeSelectorExpose.newBuilder()
        .html(it.html)
        .text(it.text)
        .pixel(it.pixel)
        .transformers(it.transformers?.map { fromInput(it) })
        .fields(it.fields?.map { fromDto(it) })
        .build()
    }
  }

  private fun fromInput(transformer: TransformerInternalOrExternalInput?): TransformerInternalOrExternal? {
    return transformer?.let {
      TransformerInternalOrExternal.newBuilder()
        .internal(fromInput(it.internal))
        .external(fromInput(it.external))
        .build()
    }
  }

  private fun fromInput(internal: InternalTransformerInput?): InternalTransformer? {
    return internal?.let {
      InternalTransformer.newBuilder()
        .transformer(it.transformer)
        .transformerData(fromInput(it.transformerData))
        .build()
    }
  }

  private fun fromInput(transformerData: MarkupTransformerDataInput?): MarkupTransformerData? {
    return transformerData?.let {
      MarkupTransformerData.newBuilder()
        .genericFeed(fromInput(it.genericFeed))
        .build()
    }
  }

  private fun fromInput(external: ExternalTransformerInput?): ExternalTransformer? {
    return external?.let {
      ExternalTransformer.newBuilder()
        .transformerId(it.transformerId)
        .transformerData(it.transformerData)
        .build()
    }
  }


  private fun fromInput(selectors: SelectorsInput?): Selectors? {
    return selectors?.let {
      Selectors.newBuilder()
        .contextXPath(it.contextXPath)
        .linkXPath(it.linkXPath)
        .dateXPath(it.dateXPath)
        .extendContext(it.extendContext)
        .dateIsStartOfEvent(it.dateIsStartOfEvent)
        .build()
    }
  }

  private fun toDto(external: ExternalTransformerInput?): ExternalTransformer? {
    return external?.let {
      ExternalTransformer.newBuilder()
        .transformerId(it.transformerId)
        .transformerData(it.transformerData)
        .build()
    }
  }

  private fun fromDto(it: ScrapeSelectorExposeFieldInput?): ScrapeSelectorExposeField? {
    return it?.let {
      ScrapeSelectorExposeField.newBuilder()
        .max(it.max)
        .min(it.min)
        .name(it.name)
        .value(fromDto(it.value))
        .nested(fromDto(it.nested))
        .build()
    }
  }

  private fun fromDto(nested: ScrapeSelectorExposeNestedFieldValueInput?): ScrapeSelectorExposeNestedFieldValue? {
    return nested?.let {
      ScrapeSelectorExposeNestedFieldValue.newBuilder()
        .fields(it.fields?.map { fromDto(it) })
        .build()
    }
  }

  private fun fromDto(value: ScrapeSelectorExposeFieldValueInput?): ScrapeSelectorExposeFieldValue? {
    return value?.let {
      ScrapeSelectorExposeFieldValue.newBuilder()
        .html(fromDto(it.html))
        .text(fromDto(it.text))
        .set(it.set)
        .build()
    }
  }

  private fun fromDto(text: ScrapeSelectorExposeFieldTextValueInput?): ScrapeSelectorExposeFieldTextValue? {
    return text?.let {
      ScrapeSelectorExposeFieldTextValue.newBuilder()
        .regex(it.regex)
        .build()
    }
  }

  private fun fromDto(html: ScrapeSelectorExposeFieldHtmlValueInput?): ScrapeSelectorExposeFieldHtmlValue? {
    return html?.let {
      ScrapeSelectorExposeFieldHtmlValue.newBuilder()
        .xpath(fromDto(it.xpath))
        .build()
    }
  }

  private fun fromDto(imageBased: ScrapeBoundingBoxInput?): ScrapeBoundingBox? {
    return imageBased?.let {
      ScrapeBoundingBox.newBuilder()
        .boundingBox(fromDto(it.boundingBox))
        .build()
    }
  }

  private fun fromDto(it: FragmentInput): Fragment {
    return Fragment.newBuilder()
      .xpath(it.xpath?.let { fromDto(it) })
      .boundingBox(it.boundingBox?.let { fromDto(it) })
      .build()
  }

  fun fromDto(it: BoundingBoxInput): BoundingBox {
    return BoundingBox.newBuilder()
      .x(it.x)
      .y(it.y)
      .w(it.w)
      .h(it.h)
      .build()
  }

  private fun fromDto(debug: ScrapeDebugOptionsInput?): ScrapeDebugOptions? {
    return debug?.let {
      ScrapeDebugOptions.newBuilder()
        .console(debug.console)
        .cookies(debug.cookies)
        .html(debug.html)
        .network(debug.network)
        .screenshot(debug.screenshot)
        .build()
    }  }

  fun fromDto(page: ScrapePageInput): ScrapePage {
    return ScrapePage.newBuilder()
      .url(page.url)
      .prerender(fromDto(page.prerender))
      .timeout(page.timeout)
      .actions(page.actions?.let { it.map { fromDto(it) } })
      .build()
  }

  private fun fromDto(it: ScrapeActionInput): ScrapeAction {
    return ScrapeAction.newBuilder()
      .type(it.type?.let { fromDto(it) })
      .cookie(it.cookie?.let { fromDto(it) })
      .select(it.select?.let { fromDto(it) })
      .click(it.click?.let { fromDto(it) })
      .header(it.header?.let { fromDto(it) })
      .wait(it.wait?.let { fromDto(it) })
      .build()
  }

  private fun fromDto(it: WaitActionInput): WaitAction {
    return WaitAction.newBuilder()
      .element(fromDto(it.element))
      .build()
  }

  private fun fromDto(it: DOMElementInput): DOMElement {
    return DOMElement.newBuilder()
      .element(it.element?.let { fromDto(it) })
      .iframe(it.iframe?.let { fromDto(it) })
      .position(it.position?.let { fromDto(it) })
      .build()
  }

  private fun fromDto(it: XYPositionInput): XYPosition {
    return XYPosition.newBuilder()
      .x(it.x)
      .y(it.y)
      .build()
  }

  private fun fromDto(it: IframeByXPathInput): IframeByXPath {
    return IframeByXPath.newBuilder()
      .xpath(fromDto(it.xpath))
      .nestedElement(fromDto(it.nestedElement))
      .build()
  }

  private fun fromDto(it: RequestHeaderInput): RequestHeader {
    return RequestHeader.newBuilder()
      .name(it.name)
      .value(it.value)
      .build()
  }

  private fun fromDto(it: DOMElementByNameOrXPathInput): DOMElementByNameOrXPath {
    return DOMElementByNameOrXPath.newBuilder()
      .name(it.name?.let { fromDto(it) })
      .xpath(it.xpath?.let { fromDto(it) })
      .build()
  }

  private fun fromDto(it: DOMElementByNameInput): DOMElementByName {
    return DOMElementByName.newBuilder()
      .value(it.value)
      .build()
  }

  private fun fromDto(it: DOMActionSelectInput): DOMActionSelect {
    return DOMActionSelect.newBuilder()
      .element(fromDto(it.element))
      .selectValue(it.selectValue)
      .build()
  }

  private fun fromDto(it: CookieValueInput): CookieValue {
    return CookieValue.newBuilder()
      .value(it.value)
      .build()
  }

  private fun fromDto(it: DOMActionTypeInput): DOMActionType {
    return DOMActionType.newBuilder()
      .element(fromDto(it.element))
      .typeValue(it.typeValue)
      .build()
  }

  private fun fromDto(it: DOMElementByXPathInput): DOMElementByXPath {
    return DOMElementByXPath.newBuilder()
      .value(it.value)
      .build()
  }

  private fun fromDto(it: ScrapePrerenderInput?): ScrapePrerender? {
    return it?.let { ScrapePrerender.newBuilder()
      .waitUntil(it.waitUntil)
      .viewport(fromDto(it.viewport))
      .build() }
  }

  private fun fromDto(it: ViewPortInput?): ViewPort? {
    return it?.let {
      ViewPort.newBuilder()
        .height(it.height)
        .width(it.width)
        .isMobile(it.isMobile)
        .isLandscape(it.isLandscape)
        .build()
    }
  }

  fun fromDto(it: ScrapeRequest): ScrapeRequest {
    return it
  }

  fun toDto(selectors: GenericFeedSelectors): Selectors {
    return Selectors.newBuilder()
      .contextXPath(selectors.contextXPath)
      .linkXPath(selectors.linkXPath)
      .extendContext(toDto(selectors.extendContext))
      .dateXPath(StringUtils.trimToEmpty(selectors.dateXPath))
      .dateIsStartOfEvent(selectors.dateIsStartOfEvent)
      .paginationXPath(StringUtils.trimToEmpty(selectors.paginationXPath))
      .build()
  }

  fun toDto(extendContext: ExtendContext): ExtendContentOptions {
    return when (extendContext) {
      ExtendContext.PREVIOUS -> ExtendContentOptions.PREVIOUS
      ExtendContext.NEXT -> ExtendContentOptions.NEXT
      ExtendContext.NONE -> ExtendContentOptions.NONE
      ExtendContext.PREVIOUS_AND_NEXT -> ExtendContentOptions.PREVIOUS_AND_NEXT
    }
  }

  fun toDto(refineOptions: GenericFeedRefineOptions): RefineOptions {
    return RefineOptions.newBuilder()
      .filter(refineOptions.filter)
      .build()
  }

  fun toSelecotrDto(it: GenericFeedRule): Selectors = Selectors.newBuilder()
    .contextXPath(it.contextXPath)
    .dateXPath(StringUtils.trimToEmpty(it.dateXPath))
    .extendContext(toDto(it.extendContext))
    .linkXPath(it.linkXPath)
    .paginationXPath(StringUtils.trimToEmpty(it.paginationXPath))
    .dateIsStartOfEvent(it.dateIsStartOfEvent)
    .build()

  fun toDto(it: RichArticle): WebDocument = WebDocument.newBuilder()
    .id(UUID.randomUUID().toString())
    .url(it.url)
    .title(it.title)
    .contentText(it.contentText)
    .description(it.contentText)
    .contentRaw(it.contentRaw)
    .contentRawMime(it.contentRawMime)
    .publishedAt(it.publishedAt.time)
    .updatedAt(it.publishedAt.time)
    .createdAt(Date().time)
    .build()

  fun toDto(it: GenericFeedRule): TransientGenericFeed {
    val selectors: Selectors = toSelecotrDto(it)
    return TransientGenericFeed.newBuilder()
      .feedUrl(it.feedUrl)
      .count(it.count)
      .hash(CryptUtil.sha1(it.feedUrl))
      .selectors(selectors)
      .score(it.score)
      .samples(it.samples.map {toDto(it) }
      ).build()
  }


}
