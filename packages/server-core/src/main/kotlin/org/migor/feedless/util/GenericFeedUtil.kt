package org.migor.feedless.util

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.generated.types.ExtendContentOptions
import org.migor.feedless.generated.types.GenericFeedSpecificationInput
import org.migor.feedless.generated.types.RefineOptions
import org.migor.feedless.generated.types.RefineOptionsInput
import org.migor.feedless.generated.types.RequestHeader
import org.migor.feedless.generated.types.RequestHeaderInput
import org.migor.feedless.generated.types.ScrapeDebugOptions
import org.migor.feedless.generated.types.ScrapeDebugOptionsInput
import org.migor.feedless.generated.types.ScrapePage
import org.migor.feedless.generated.types.ScrapePageInput
import org.migor.feedless.generated.types.ScrapePrerender
import org.migor.feedless.generated.types.ScrapePrerenderInput
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ScrapeRequestInput
import org.migor.feedless.generated.types.Selectors
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.generated.types.TransientGenericFeed
import org.migor.feedless.generated.types.ViewPort
import org.migor.feedless.generated.types.ViewPortInput
import org.migor.feedless.generated.types.WebDocument
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
      .emit(it.emit)
      .page(fromDto(it.page))
      .elements(it.elements)
      .debug(fromDto(it.debug))
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
      .cookie(page.cookie)
      .prerender(fromDto(page.prerender))
      .timeout(page.timeout)
      .headers(page.headers?.map { fromDto(it) })
      .build()
  }

  private fun fromDto(it: ScrapePrerenderInput?): ScrapePrerender? {
    return it?.let { ScrapePrerender.newBuilder()
      .evalScript(it.evalScript)
      .waitUntil(it.waitUntil)
      .evalScriptTimeout(it.evalScriptTimeout)
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

  private fun fromDto(it: RequestHeaderInput): RequestHeader {
    return RequestHeader.newBuilder()
      .name(it.name)
      .value(it.value)
      .build()
  }

  private fun fromDto(waitUntil: PuppeteerWaitUntilDto?): PuppeteerWaitUntil {
    return when (waitUntil) {
      PuppeteerWaitUntilDto.domcontentloaded -> PuppeteerWaitUntil.domcontentloaded
      PuppeteerWaitUntilDto.networkidle0 -> PuppeteerWaitUntil.networkidle0
      PuppeteerWaitUntilDto.networkidle2 -> PuppeteerWaitUntil.networkidle2
      else -> PuppeteerWaitUntil.load
    }
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
