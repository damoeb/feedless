package org.migor.feedless.api

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

fun EntityVisibility.toDto(): Visibility = when (this) {
  EntityVisibility.isPrivate -> Visibility.isPrivate
  EntityVisibility.isPublic -> Visibility.isPublic
}

fun ScrapeResponseInput.fromDto(): ScrapeResponse = ScrapeResponse.newBuilder()
  .url(url)
  .debug(fromDto(debug))
  .errorMessage(errorMessage)
  .failed(failed)
  .elements(elements?.map { it.fromDto() })
  .build()


fun RemoteNativeFeedRef.toDto(): NativeFeed = NativeFeed.newBuilder()
  .feedUrl(url)
  .title(title)
  .description(description)
  .build()

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
    .samples(samples.map { it.toDto() }
    ).build()
}

private fun ScrapedElementInput.fromDto(): ScrapedElement = ScrapedElement.newBuilder()
  .selector(selector?.fromDto())
  .image(image?.fromDto())
  .build()

private fun ScrapedByBoundingBoxInput.fromDto(): ScrapedByBoundingBox = ScrapedByBoundingBox.newBuilder()
  .data(data.fromDto())
  .boundingBox(boundingBox.fromDto())
  .build()

private fun Base64DataInput.fromDto(): Base64Data = Base64Data.newBuilder()
  .base64Data(base64Data)
  .build()

private fun ScrapedBySelectorInput.fromDto(): ScrapedBySelector = ScrapedBySelector.newBuilder()
  .html(html?.fromDto())
  .text(text?.fromDto())
  .pixel(pixel?.fromDto())
  .xpath(xpath?.fromDto())
  .build()

private fun TextDataInput.fromDto(): TextData = TextData.newBuilder()
  .data(data)
  .build()

private fun fromDto(it: ScrapeDebugResponseInput): ScrapeDebugResponse = ScrapeDebugResponse.newBuilder()
  .corrId(it.corrId)
  .console(it.console)
  .network(it.network.map { it.fromDto() })
  .html(it.html)
  .contentType(it.contentType)
  .statusCode(it.statusCode)
  .screenshot(it.screenshot)
  .prerendered(it.prerendered)
  .viewport(it.viewport?.fromDto())
  .metrics(it.metrics?.fromDto())
  .build()

private fun ScrapeDebugTimesInput.fromDto(): ScrapeDebugTimes = ScrapeDebugTimes.newBuilder()
  .queue(queue)
  .render(render)
  .build()

private fun NetworkRequestInput.fromDto(): NetworkRequest = NetworkRequest.newBuilder()
  .responseBody(responseBody)
  .responseHeaders(responseHeaders)
  .responseSize(responseSize)
  .requestHeaders(requestHeaders)
  .requestPostData(requestPostData)
  .build()

fun Selectors.fromDto(): GenericFeedSelectors = GenericFeedSelectors(
  linkXPath = linkXPath,
  extendContext = fromDto(extendContext),
  contextXPath = contextXPath,
  dateXPath = dateXPath,
  dateIsStartOfEvent = BooleanUtils.isTrue(dateIsStartOfEvent)
)

fun ScrapeRequestInput.fromDto(): ScrapeRequest = ScrapeRequest.newBuilder()
  .emit(emit?.map { it.fromDto() })
  .page(page.fromDto())
  .debug(debug?.fromDto())
  .tags(tags)
  .build()

private fun fromDto(extendContext: ExtendContentOptions?): ExtendContext = when (extendContext) {
  ExtendContentOptions.NEXT -> ExtendContext.NEXT
  ExtendContentOptions.PREVIOUS -> ExtendContext.PREVIOUS
  else -> ExtendContext.NONE
}

private fun ScrapeEmitInput.fromDto(): ScrapeEmit = ScrapeEmit.newBuilder()
  .imageBased(imageBased?.fromDto())
  .selectorBased(selectorBased?.fromDto())
  .build()

private fun ScrapeSelectorInput.fromDto(): ScrapeSelector = ScrapeSelector.newBuilder()
  .xpath(xpath.fromDto())
  .expose(expose?.fromDto())
  .max(max)
  .min(min)
  .build()

private fun ScrapeSelectorExposeInput.fromDto(): ScrapeSelectorExpose = ScrapeSelectorExpose.newBuilder()
  .pixel(pixel)
  .transformers(transformers?.map { it.fromDto() })
  .fields(fields?.map { it.fromDto() })
  .build()

private fun PluginExecutionInput.fromDto(): PluginExecution = PluginExecution.newBuilder()
  .pluginId(pluginId)
  .params(params?.fromDto())
  .build()

private fun PluginExecutionParamsInput.fromDto(): PluginExecutionParams = PluginExecutionParams.newBuilder()
  .org_feedless_feed(org_feedless_feed?.fromDto())
  .jsonData(jsonData)
  .org_feedless_diff_email_forward(org_feedless_diff_email_forward?.fromDto())
  .org_feedless_fulltext(org_feedless_fulltext?.fromDto())
  .build()

private fun FeedParamsInput.fromDto(): FeedParams {
  return FeedParams.newBuilder()
    .generic(generic?.fromDto())
    .build()
}

private fun FulltextPluginParamsInput.fromDto(): FulltextPluginParams = FulltextPluginParams.newBuilder()
  .readability(readability)
  .build()

private fun SelectorsInput.fromDto(): Selectors = Selectors.newBuilder()
  .contextXPath(contextXPath)
  .linkXPath(linkXPath)
  .dateXPath(dateXPath)
  .extendContext(extendContext)
  .dateIsStartOfEvent(BooleanUtils.isTrue(dateIsStartOfEvent))
  .build()

private fun ScrapeSelectorExposeFieldInput.fromDto(): ScrapeSelectorExposeField = ScrapeSelectorExposeField.newBuilder()
  .max(max)
  .min(min)
  .name(name)
  .value(value?.fromDto())
  .nested(nested?.fromDto())
  .build()

private fun ScrapeSelectorExposeNestedFieldValueInput.fromDto(): ScrapeSelectorExposeNestedFieldValue {
  return ScrapeSelectorExposeNestedFieldValue.newBuilder()
    .fields(fields?.map { it.fromDto() })
    .build()
}

private fun ScrapeSelectorExposeFieldValueInput.fromDto(): ScrapeSelectorExposeFieldValue? {
  return ScrapeSelectorExposeFieldValue.newBuilder()
    .html(html?.fromDto())
    .text(text?.fromDto())
    .set(set)
    .build()
}

private fun ScrapeSelectorExposeFieldTextValueInput.fromDto(): ScrapeSelectorExposeFieldTextValue =
  ScrapeSelectorExposeFieldTextValue.newBuilder()
    .regex(regex)
    .build()

private fun ScrapeSelectorExposeFieldHtmlValueInput.fromDto(): ScrapeSelectorExposeFieldHtmlValue =
  ScrapeSelectorExposeFieldHtmlValue.newBuilder()
    .xpath(xpath.fromDto())
    .build()

private fun ScrapeBoundingBoxInput.fromDto(): ScrapeBoundingBox = ScrapeBoundingBox.newBuilder()
  .boundingBox(boundingBox.fromDto())
  .build()

private fun BoundingBoxInput.fromDto(): BoundingBox = BoundingBox.newBuilder()
  .x(x)
  .y(y)
  .w(w)
  .h(h)
  .build()

private fun ScrapeDebugOptionsInput.fromDto(): ScrapeDebugOptions = ScrapeDebugOptions.newBuilder()
  .console(console)
  .html(html)
  .network(network)
  .screenshot(screenshot)
  .build()

private fun ScrapePageInput.fromDto(): ScrapePage = ScrapePage.newBuilder()
  .url(url)
  .prerender(prerender?.fromDto())
  .timeout(timeout)
  .actions(actions?.map { it.fromDto() })
  .build()

private fun ScrapeActionInput.fromDto(): ScrapeAction = ScrapeAction.newBuilder()
  .type(type?.fromDto())
  .purge(purge?.fromDto())
  .select(select?.fromDto())
  .click(click?.fromDto())
  .header(header?.fromDto())
  .wait(wait?.fromDto())
  .build()

private fun WaitActionInput.fromDto(): WaitAction = WaitAction.newBuilder()
  .element(element.fromDto())
  .build()

private fun DOMElementInput.fromDto(): DOMElement = DOMElement.newBuilder()
  .element(element?.fromDto())
  .position(position?.fromDto())
  .build()

private fun XYPositionInput.fromDto(): XYPosition = XYPosition.newBuilder()
  .x(x)
  .y(y)
  .build()

private fun RequestHeaderInput.fromDto(): RequestHeader = RequestHeader.newBuilder()
  .name(name)
  .value(value)
  .build()

private fun DOMElementByNameOrXPathInput.fromDto(): DOMElementByNameOrXPath = DOMElementByNameOrXPath.newBuilder()
  .name(name?.fromDto())
  .xpath(xpath?.fromDto())
  .build()

private fun DOMElementByNameInput.fromDto(): DOMElementByName = DOMElementByName.newBuilder()
  .value(value)
  .build()

private fun DOMActionSelectInput.fromDto(): DOMActionSelect = DOMActionSelect.newBuilder()
  .element(element.fromDto())
  .selectValue(selectValue)
  .build()

private fun DOMActionTypeInput.fromDto(): DOMActionType = DOMActionType.newBuilder()
  .element(element.fromDto())
  .typeValue(typeValue)
  .build()

private fun DOMElementByXPathInput.fromDto(): DOMElementByXPath = DOMElementByXPath.newBuilder()
  .value(value)
  .build()

private fun ScrapePrerenderInput.fromDto(): ScrapePrerender = ScrapePrerender.newBuilder()
  .waitUntil(waitUntil)
  .language(language)
  .viewport(viewport?.fromDto())
  .additionalWaitSec(additionalWaitSec)
  .build()

private fun ViewPortInput.fromDto(): ViewPort = ViewPort.newBuilder()
  .height(height)
  .width(width)
  .isMobile(isMobile)
  .isLandscape(isLandscape)
  .build()

private fun ExtendContext.toDto(): ExtendContentOptions {
  return when (this) {
    ExtendContext.PREVIOUS -> ExtendContentOptions.PREVIOUS
    ExtendContext.NEXT -> ExtendContentOptions.NEXT
    ExtendContext.NONE -> ExtendContentOptions.NONE
    ExtendContext.PREVIOUS_AND_NEXT -> ExtendContentOptions.PREVIOUS_AND_NEXT
  }
}

private fun RichArticle.toDto(): WebDocument {
  val builder = WebDocument.newBuilder()
    .id(UUID.randomUUID().toString())
    .url(url)
    .contentText(contentText)
    .publishedAt(publishedAt.time)
    .startingAt(startingAt?.time)
    .updatedAt(publishedAt.time)
    .createdAt(Date().time)

  return if (isHtml(contentRawMime)) {
    try {
      builder.contentHtml(String(Base64.getDecoder().decode(contentRawBase64)))
    } catch (e: Exception) {
      builder.contentHtml(contentRawBase64)
    }.build()
  } else {
    builder
      .contentRawBase64(contentRawBase64)
       .contentRawMime(contentRawMime)
      .build()
  }
}


fun isHtml(contentRawMime: String?): Boolean = contentRawMime?.lowercase()?.startsWith("text/html") == true

private fun DiffEmailForwardParamsInput.fromDto(): DiffEmailForwardParams = DiffEmailForwardParams.newBuilder()
  .inlineLatestImage(inlineLatestImage)
  .inlineDiffImage(inlineDiffImage)
  .inlinePreviousImage(inlinePreviousImage)
  .nextItemMinIncrement(nextItemMinIncrement)
  .compareBy(compareBy.fromDto())
  .build()

private fun CompareByInput.fromDto(): CompareBy {
  return CompareBy.newBuilder()
    .field(field)
    .fragmentNameRef(fragmentNameRef)
    .build()
}
