package org.migor.feedless.api.graphql

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.feed.discovery.RemoteNativeFeedRef
import org.migor.feedless.generated.types.*
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.web.ExtendContext
import org.migor.feedless.web.GenericFeedRule
import org.migor.feedless.web.GenericFeedSelectors
import java.util.*
import org.migor.feedless.generated.types.Visibility as VisibilityDto

object DtoResolver {

  fun EntityVisibility.toDto(): VisibilityDto = when (this) {
    EntityVisibility.isPrivate -> VisibilityDto.isPrivate
    EntityVisibility.isPublic -> VisibilityDto.isPublic
  }

  fun fromDto(visibility: VisibilityDto?): EntityVisibility = when (visibility) {
    VisibilityDto.isPublic -> EntityVisibility.isPublic
    else -> EntityVisibility.isPrivate
  }

  fun ScrapeResponseInput.fromDto(): ScrapeResponse {
    return ScrapeResponse.newBuilder()
      .url(this.url)
      .debug(fromDto(this.debug))
      .errorMessage(this.errorMessage)
      .failed(this.failed)
      .elements(this.elements?.map { it.fromDto() })
      .build()
  }

  fun RemoteNativeFeedRef.toDto(): NativeFeed {
    return NativeFeed.newBuilder()
      .feedUrl(this.url)
      .title(this.title)
      .description(this.description)
      .build()
  }
  fun GenericFeedRule.toDto(): TransientGenericFeed {
    val selectors: Selectors = Selectors.newBuilder()
      .contextXPath(this.contextXPath)
      .dateXPath(StringUtils.trimToEmpty(this.dateXPath))
      .extendContext(this.extendContext.toDto())
      .linkXPath(this.linkXPath)
      .dateIsStartOfEvent(this.dateIsStartOfEvent)
      .build()

    return TransientGenericFeed.newBuilder()
      .feedUrl(this.feedUrl)
      .count(this.count)
      .hash(CryptUtil.sha1(this.feedUrl))
      .selectors(selectors)
      .score(this.score)
      .samples(this.samples.map { toDto(it) }
      ).build()
  }

  private fun ScrapedElementInput.fromDto(): ScrapedElement {
    return ScrapedElement.newBuilder()
      .selector(this.selector?.fromDto())
      .image(this.image?.fromDto())
      .build()
  }

  private fun ScrapedByBoundingBoxInput.fromDto(): ScrapedByBoundingBox {
    return ScrapedByBoundingBox.newBuilder()
      .data(this.data.fromDto())
      .boundingBox(this.boundingBox.fromDto())
      .build()
  }

  private fun Base64DataInput.fromDto(): Base64Data {
    return Base64Data.newBuilder()
      .base64Data(this.base64Data)
      .build()
  }

  private fun ScrapedBySelectorInput.fromDto(): ScrapedBySelector {
    return ScrapedBySelector.newBuilder()
      .html(this.html?.fromDto())
      .text(this.text?.fromDto())
      .pixel(this.pixel?.fromDto())
      .xpath(this.xpath?.fromDto())
      .build()

  }

  private fun TextDataInput.fromDto(): TextData {
    return TextData.newBuilder()
      .data(this.data)
      .build()
  }

  private fun fromDto(it: ScrapeDebugResponseInput): ScrapeDebugResponse {
    return ScrapeDebugResponse.newBuilder()
      .corrId(it.corrId)
      .console(it.console)
      .cookies(it.cookies)
      .network(it.network.map { it.fromDto() })
      .html(it.html)
      .contentType(it.contentType)
      .statusCode(it.statusCode)
      .screenshot(it.screenshot)
      .prerendered(it.prerendered)
      .viewport(it.viewport?.fromDto())
      .metrics(it.metrics?.fromDto())
      .build()
  }

  private fun ScrapeDebugTimesInput.fromDto(): ScrapeDebugTimes {
    return ScrapeDebugTimes.newBuilder()
      .queue(this.queue)
      .render(this.render)
      .build()
  }

  private fun NetworkRequestInput.fromDto(): NetworkRequest {
    return NetworkRequest.newBuilder()
      .responseBody(this.responseBody)
      .responseHeaders(this.responseHeaders)
      .responseSize(this.responseSize)
      .requestHeaders(this.requestHeaders)
      .requestPostData(this.requestPostData)
      .build()
  }

  fun Selectors.fromDto(): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = this.linkXPath,
      extendContext = fromDto(this.extendContext),
      contextXPath = this.contextXPath,
      dateXPath = this.dateXPath,
      dateIsStartOfEvent = BooleanUtils.isTrue(this.dateIsStartOfEvent)
    )
  }

  fun ScrapeRequestInput.fromDto(): ScrapeRequest {
    return ScrapeRequest.newBuilder()
      .emit(this.emit?.map { it.fromDto() })
      .page(this.page.fromDto())
      .debug(this.debug?.fromDto())
      .build()
  }

  private fun fromDto(extendContext: ExtendContentOptions?): ExtendContext {
    return when (extendContext) {
      ExtendContentOptions.NEXT -> ExtendContext.NEXT
      ExtendContentOptions.PREVIOUS -> ExtendContext.PREVIOUS
      ExtendContentOptions.NONE -> ExtendContext.NONE
      else -> throw RuntimeException("ExtendContentOptionsDto $extendContext is not supported")
    }
  }

  private fun ScrapeEmitInput.fromDto(): ScrapeEmit {
    return ScrapeEmit.newBuilder()
      .imageBased(this.imageBased?.fromDto())
      .selectorBased(this.selectorBased?.fromDto())
      .build()
  }

  private fun ScrapeSelectorInput.fromDto(): ScrapeSelector {
    return ScrapeSelector.newBuilder()
      .xpath(this.xpath.fromDto())
      .expose(this.expose?.fromDto())
      .max(this.max)
      .min(this.min)
      .build()
  }

  private fun ScrapeSelectorExposeInput.fromDto(): ScrapeSelectorExpose {
    return ScrapeSelectorExpose.newBuilder()
      .pixel(this.pixel)
      .transformers(this.transformers?.map { it.fromDto() })
      .fields(this.fields?.map { it.fromDto() })
      .build()
  }

  private fun TransformerInternalOrExternalInput.fromDto(): TransformerInternalOrExternal? {
    return TransformerInternalOrExternal.newBuilder()
      .internal(this.internal?.fromDto())
      .external(this.external?.fromDto())
      .build()
  }

  private fun InternalTransformerInput.fromDto(): InternalTransformer {
    return InternalTransformer.newBuilder()
      .transformer(this.transformer)
      .transformerData(this.transformerData?.fromDto())
      .build()
  }

  private fun MarkupTransformerDataInput.fromDto(): MarkupTransformerData {
    return MarkupTransformerData.newBuilder()
      .genericFeed(this.genericFeed.fromDto())
      .build()
  }

  private fun ExternalTransformerInput.fromDto(): ExternalTransformer {
    return ExternalTransformer.newBuilder()
      .transformerId(this.transformerId)
      .transformerData(this.transformerData)
      .build()
  }

  private fun SelectorsInput.fromDto(): Selectors {
    return Selectors.newBuilder()
      .contextXPath(this.contextXPath)
      .linkXPath(this.linkXPath)
      .dateXPath(this.dateXPath)
      .extendContext(this.extendContext)
      .dateIsStartOfEvent(this.dateIsStartOfEvent)
      .build()

  }

  private fun ScrapeSelectorExposeFieldInput.fromDto(): ScrapeSelectorExposeField {
    return ScrapeSelectorExposeField.newBuilder()
      .max(this.max)
      .min(this.min)
      .name(this.name)
      .value(fromDto(value))
      .nested(fromDto(nested))
      .build()
  }

  private fun fromDto(nested: ScrapeSelectorExposeNestedFieldValueInput?): ScrapeSelectorExposeNestedFieldValue? {
    return nested?.let {
      ScrapeSelectorExposeNestedFieldValue.newBuilder()
        .fields(it.fields?.map { it.fromDto() })
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
        .xpath(it.xpath.fromDto())
        .build()
    }
  }

  private fun ScrapeBoundingBoxInput.fromDto(): ScrapeBoundingBox? {
    return ScrapeBoundingBox.newBuilder()
      .boundingBox(boundingBox.fromDto())
      .build()
  }

  private fun BoundingBoxInput.fromDto(): BoundingBox {
    return BoundingBox.newBuilder()
      .x(this.x)
      .y(this.y)
      .w(this.w)
      .h(this.h)
      .build()
  }

  private fun ScrapeDebugOptionsInput.fromDto(): ScrapeDebugOptions? {
    return ScrapeDebugOptions.newBuilder()
      .console(this.console)
      .cookies(this.cookies)
      .html(this.html)
      .network(this.network)
      .screenshot(this.screenshot)
      .build()
  }

  private fun ScrapePageInput.fromDto(): ScrapePage {
    return ScrapePage.newBuilder()
      .url(this.url)
      .prerender(this.prerender?.fromDto())
      .timeout(this.timeout)
      .actions(this.actions?.let { it.map { fromDto(it) } })
      .build()
  }

  private fun fromDto(it: ScrapeActionInput): ScrapeAction {
    return ScrapeAction.newBuilder()
      .type(it.type?.fromDto())
      .cookie(it.cookie?.fromDto())
      .select(it.select?.fromDto())
      .click(it.click?.fromDto())
      .header(it.header?.fromDto())
      .wait(it.wait?.fromDto())
      .build()
  }

  private fun WaitActionInput.fromDto(): WaitAction {
    return WaitAction.newBuilder()
      .element(this.element.fromDto())
      .build()
  }

  private fun DOMElementInput.fromDto(): DOMElement {
    return DOMElement.newBuilder()
      .element(this.element?.fromDto())
      .iframe(this.iframe?.fromDto())
      .position(this.position?.fromDto())
      .build()
  }

  private fun XYPositionInput.fromDto(): XYPosition {
    return XYPosition.newBuilder()
      .x(this.x)
      .y(this.y)
      .build()
  }

  private fun IframeByXPathInput.fromDto(): IframeByXPath {
    return IframeByXPath.newBuilder()
      .xpath(this.xpath.fromDto())
      .nestedElement(this.nestedElement?.fromDto())
      .build()
  }

  private fun RequestHeaderInput.fromDto(): RequestHeader {
    return RequestHeader.newBuilder()
      .name(this.name)
      .value(this.value)
      .build()
  }

  private fun DOMElementByNameOrXPathInput.fromDto(): DOMElementByNameOrXPath {
    return DOMElementByNameOrXPath.newBuilder()
      .name(this.name?.fromDto())
      .xpath(this.xpath?.fromDto())
      .build()
  }

  private fun DOMElementByNameInput.fromDto(): DOMElementByName {
    return DOMElementByName.newBuilder()
      .value(this.value)
      .build()
  }

  private fun DOMActionSelectInput.fromDto(): DOMActionSelect {
    return DOMActionSelect.newBuilder()
      .element(this.element.fromDto())
      .selectValue(this.selectValue)
      .build()
  }

  private fun CookieValueInput.fromDto(): CookieValue {
    return CookieValue.newBuilder()
      .value(this.value)
      .build()
  }

  private fun DOMActionTypeInput.fromDto(): DOMActionType {
    return DOMActionType.newBuilder()
      .element(this.element.fromDto())
      .typeValue(this.typeValue)
      .build()
  }

  private fun DOMElementByXPathInput.fromDto(): DOMElementByXPath {
    return DOMElementByXPath.newBuilder()
      .value(this.value)
      .build()
  }

  private fun ScrapePrerenderInput.fromDto(): ScrapePrerender? {
    return ScrapePrerender.newBuilder()
      .waitUntil(this.waitUntil)
      .viewport(this.viewport?.fromDto())
      .build()
  }

  private fun ViewPortInput.fromDto(): ViewPort? {
    return ViewPort.newBuilder()
      .height(this.height)
      .width(this.width)
      .isMobile(this.isMobile)
      .isLandscape(this.isLandscape)
      .build()
  }

  private fun ExtendContext.toDto(): ExtendContentOptions {
    return when (this) {
      ExtendContext.PREVIOUS -> ExtendContentOptions.PREVIOUS
      ExtendContext.NEXT -> ExtendContentOptions.NEXT
      ExtendContext.NONE -> ExtendContentOptions.NONE
      ExtendContext.PREVIOUS_AND_NEXT -> ExtendContentOptions.PREVIOUS_AND_NEXT
    }
  }

  private fun toDto(it: RichArticle): WebDocument = WebDocument.newBuilder()
    .id(UUID.randomUUID().toString())
    .url(it.url)
    .contentText(it.contentText)
    .contentRaw(it.contentRaw)
    .contentRawMime(it.contentRawMime)
    .publishedAt(it.publishedAt.time)
    .updatedAt(it.publishedAt.time)
    .createdAt(Date().time)
    .build()

}
