package org.migor.feedless.api.graphql

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.feed.discovery.RemoteNativeFeedRef
import org.migor.feedless.generated.types.*
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

  fun ScrapeResponseInput.fromDto(): ScrapeResponse {
    return ScrapeResponse.newBuilder()
      .url(url)
      .debug(fromDto(debug))
      .errorMessage(errorMessage)
      .failed(failed)
      .elements(elements?.map { it.fromDto() })
      .build()
  }

  fun RemoteNativeFeedRef.toDto(): NativeFeed {
    return NativeFeed.newBuilder()
      .feedUrl(url)
      .title(title)
      .description(description)
      .build()
  }

  fun GenericFeedRule.toDto(): TransientGenericFeed {
    val selectors: Selectors = Selectors.newBuilder()
      .contextXPath(contextXPath)
      .dateXPath(StringUtils.trimToEmpty(dateXPath))
      .extendContext(extendContext.toDto())
      .linkXPath(linkXPath)
      .dateIsStartOfEvent(dateIsStartOfEvent)
      .build()

    return TransientGenericFeed.newBuilder()
      .feedUrl(feedUrl)
      .count(count)
      .hash(CryptUtil.sha1(feedUrl))
      .selectors(selectors)
      .score(score)
      .samples(samples.map { toDto(it) }
      ).build()
  }

  private fun ScrapedElementInput.fromDto(): ScrapedElement {
    return ScrapedElement.newBuilder()
      .selector(selector?.fromDto())
      .image(image?.fromDto())
      .build()
  }

  private fun ScrapedByBoundingBoxInput.fromDto(): ScrapedByBoundingBox {
    return ScrapedByBoundingBox.newBuilder()
      .data(data.fromDto())
      .boundingBox(boundingBox.fromDto())
      .build()
  }

  private fun Base64DataInput.fromDto(): Base64Data {
    return Base64Data.newBuilder()
      .base64Data(base64Data)
      .build()
  }

  private fun ScrapedBySelectorInput.fromDto(): ScrapedBySelector {
    return ScrapedBySelector.newBuilder()
      .html(html?.fromDto())
      .text(text?.fromDto())
      .pixel(pixel?.fromDto())
      .xpath(xpath?.fromDto())
      .build()

  }

  private fun TextDataInput.fromDto(): TextData {
    return TextData.newBuilder()
      .data(data)
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
      .queue(queue)
      .render(render)
      .build()
  }

  private fun NetworkRequestInput.fromDto(): NetworkRequest {
    return NetworkRequest.newBuilder()
      .responseBody(responseBody)
      .responseHeaders(responseHeaders)
      .responseSize(responseSize)
      .requestHeaders(requestHeaders)
      .requestPostData(requestPostData)
      .build()
  }

  fun Selectors.fromDto(): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = linkXPath,
      extendContext = fromDto(extendContext),
      contextXPath = contextXPath,
      dateXPath = dateXPath,
      dateIsStartOfEvent = BooleanUtils.isTrue(dateIsStartOfEvent)
    )
  }

  fun ScrapeRequestInput.fromDto(): ScrapeRequest {
    return ScrapeRequest.newBuilder()
      .emit(emit?.map { it.fromDto() })
      .page(page.fromDto())
      .debug(debug?.fromDto())
      .build()
  }

  private fun fromDto(extendContext: ExtendContentOptions?): ExtendContext {
    return when (extendContext) {
      ExtendContentOptions.NEXT -> ExtendContext.NEXT
      ExtendContentOptions.PREVIOUS -> ExtendContext.PREVIOUS
      else -> ExtendContext.NONE
    }
  }

  private fun ScrapeEmitInput.fromDto(): ScrapeEmit {
    return ScrapeEmit.newBuilder()
      .imageBased(imageBased?.fromDto())
      .selectorBased(selectorBased?.fromDto())
      .build()
  }

  private fun ScrapeSelectorInput.fromDto(): ScrapeSelector {
    return ScrapeSelector.newBuilder()
      .xpath(xpath.fromDto())
      .expose(expose?.fromDto())
      .max(max)
      .min(min)
      .build()
  }

  private fun ScrapeSelectorExposeInput.fromDto(): ScrapeSelectorExpose {
    return ScrapeSelectorExpose.newBuilder()
      .pixel(pixel)
      .transformers(transformers?.map { it.fromDto() })
      .fields(fields?.map { it.fromDto() })
      .build()
  }

  private fun PluginExecutionInput.fromDto(): PluginExecution {
    return PluginExecution.newBuilder()
      .pluginId(pluginId)
      .params(params?.fromDto())
      .build()
  }

  private fun PluginExecutionParamsInput.fromDto(): PluginExecutionParams {
    return PluginExecutionParams.newBuilder()
      .genericFeed(genericFeed?.fromDto())
      .rawJson(rawJson)
      .diffEmailForward(diffEmailForward?.fromDto())
      .fulltext(fulltext?.fromDto())
      .build()
  }

  private fun FulltextPluginParamsInput.fromDto(): FulltextPluginParams {
    return FulltextPluginParams.newBuilder()
      .readability(readability)
      .build()
  }

  private fun SelectorsInput.fromDto(): Selectors {
    return Selectors.newBuilder()
      .contextXPath(contextXPath)
      .linkXPath(linkXPath)
      .dateXPath(dateXPath)
      .extendContext(extendContext)
      .dateIsStartOfEvent(BooleanUtils.isTrue(dateIsStartOfEvent))
      .build()

  }

  private fun ScrapeSelectorExposeFieldInput.fromDto(): ScrapeSelectorExposeField {
    return ScrapeSelectorExposeField.newBuilder()
      .max(max)
      .min(min)
      .name(name)
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
      .x(x)
      .y(y)
      .w(w)
      .h(h)
      .build()
  }

  private fun ScrapeDebugOptionsInput.fromDto(): ScrapeDebugOptions? {
    return ScrapeDebugOptions.newBuilder()
      .console(console)
      .cookies(cookies)
      .html(html)
      .network(network)
      .screenshot(screenshot)
      .build()
  }

  private fun ScrapePageInput.fromDto(): ScrapePage {
    return ScrapePage.newBuilder()
      .url(url)
      .prerender(prerender?.fromDto())
      .timeout(timeout)
      .actions(actions?.map { it.fromDto() })
      .build()
  }

  private fun ScrapeActionInput.fromDto(): ScrapeAction {
    return ScrapeAction.newBuilder()
      .type(type?.fromDto())
      .cookie(cookie?.fromDto())
      .purge(purge?.fromDto())
      .select(select?.fromDto())
      .click(click?.fromDto())
      .header(header?.fromDto())
      .wait(wait?.fromDto())
      .build()
  }

  private fun WaitActionInput.fromDto(): WaitAction {
    return WaitAction.newBuilder()
      .element(element.fromDto())
      .build()
  }

  private fun DOMElementInput.fromDto(): DOMElement {
    return DOMElement.newBuilder()
      .element(element?.fromDto())
      .iframe(iframe?.fromDto())
      .position(position?.fromDto())
      .build()
  }

  private fun XYPositionInput.fromDto(): XYPosition {
    return XYPosition.newBuilder()
      .x(x)
      .y(y)
      .build()
  }

  private fun IframeByXPathInput.fromDto(): IframeByXPath {
    return IframeByXPath.newBuilder()
      .xpath(xpath.fromDto())
      .nestedElement(nestedElement?.fromDto())
      .build()
  }

  private fun RequestHeaderInput.fromDto(): RequestHeader {
    return RequestHeader.newBuilder()
      .name(name)
      .value(value)
      .build()
  }

  private fun DOMElementByNameOrXPathInput.fromDto(): DOMElementByNameOrXPath {
    return DOMElementByNameOrXPath.newBuilder()
      .name(name?.fromDto())
      .xpath(xpath?.fromDto())
      .build()
  }

  private fun DOMElementByNameInput.fromDto(): DOMElementByName {
    return DOMElementByName.newBuilder()
      .value(value)
      .build()
  }

  private fun DOMActionSelectInput.fromDto(): DOMActionSelect {
    return DOMActionSelect.newBuilder()
      .element(element.fromDto())
      .selectValue(selectValue)
      .build()
  }

  private fun CookieValueInput.fromDto(): CookieValue {
    return CookieValue.newBuilder()
      .value(value)
      .build()
  }

  private fun DOMActionTypeInput.fromDto(): DOMActionType {
    return DOMActionType.newBuilder()
      .element(element.fromDto())
      .typeValue(typeValue)
      .build()
  }

  private fun DOMElementByXPathInput.fromDto(): DOMElementByXPath {
    return DOMElementByXPath.newBuilder()
      .value(value)
      .build()
  }

  private fun ScrapePrerenderInput.fromDto(): ScrapePrerender? {
    return ScrapePrerender.newBuilder()
      .waitUntil(waitUntil)
      .language(language)
      .viewport(viewport?.fromDto())
      .additionalWaitSec(additionalWaitSec)
      .build()
  }

  private fun ViewPortInput.fromDto(): ViewPort? {
    return ViewPort.newBuilder()
      .height(height)
      .width(width)
      .isMobile(isMobile)
      .isLandscape(isLandscape)
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
    .contentRawBase64(it.contentRawBase64)
    .contentRawMime(it.contentRawMime)
    .publishedAt(it.publishedAt.time)
    .updatedAt(it.publishedAt.time)
    .createdAt(Date().time)
    .build()

}

private fun DiffEmailForwardParamsInput.fromDto(): DiffEmailForwardParams {
  return DiffEmailForwardParams.newBuilder()
    .inlineLatestImage(inlineLatestImage)
    .inlineDiffImage(inlineDiffImage)
    .inlinePreviousImage(inlinePreviousImage)
    .nextItemMinIncrement(nextItemMinIncrement)
    .compareBy(compareBy)
    .build()
}
