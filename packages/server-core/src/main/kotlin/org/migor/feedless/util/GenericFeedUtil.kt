package org.migor.feedless.util

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.generated.types.ExtendContentOptions
import org.migor.feedless.generated.types.FetchOptionsInput
import org.migor.feedless.generated.types.GenericFeedSpecificationInput
import org.migor.feedless.generated.types.RefineOptions
import org.migor.feedless.generated.types.RefineOptionsInput
import org.migor.feedless.generated.types.Selectors
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.generated.types.TransientGenericFeed
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.web.ExtendContext
import org.migor.feedless.web.FetchOptions
import org.migor.feedless.web.GenericFeedParserOptions
import org.migor.feedless.web.GenericFeedRefineOptions
import org.migor.feedless.web.GenericFeedRule
import org.migor.feedless.web.GenericFeedSelectors
import org.migor.feedless.web.GenericFeedSpecification
import org.migor.feedless.web.PuppeteerWaitUntil
import java.util.*
import org.migor.feedless.generated.types.FetchOptions as FetchOptionsDto
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
      fetchOptions = fromDto(specification.fetchOptions),
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

  fun fromDto(fetchOptions: FetchOptionsDto): FetchOptions {
    return FetchOptions(
      websiteUrl = fetchOptions.websiteUrl,
      prerender = fetchOptions.prerender,
      prerenderWaitUntil = fromDto(fetchOptions.prerenderWaitUntil),
      prerenderScript = StringUtils.trimToEmpty(fetchOptions.prerenderScript)
    )
  }

  fun fromDto(fetchOptions: FetchOptionsInput): FetchOptions {
    return FetchOptions(
      websiteUrl = fetchOptions.websiteUrl,
      prerender = fetchOptions.prerender,
      prerenderWaitUntil = fromDto(fetchOptions.prerenderWaitUntil),
      prerenderScript = StringUtils.trimToEmpty(fetchOptions.prerenderScript)
    )
  }

  private fun fromDto(waitUntil: PuppeteerWaitUntilDto?): PuppeteerWaitUntil {
    return when (waitUntil) {
      PuppeteerWaitUntilDto.domcontentloaded -> PuppeteerWaitUntil.domcontentloaded
      PuppeteerWaitUntilDto.networkidle0 -> PuppeteerWaitUntil.networkidle0
      PuppeteerWaitUntilDto.networkidle2 -> PuppeteerWaitUntil.networkidle2
      else -> PuppeteerWaitUntil.load
    }
  }

  fun toDto(fetchOptions: FetchOptionsInput): FetchOptionsDto {
    return FetchOptionsDto.newBuilder()
      .websiteUrl(fetchOptions.websiteUrl)
      .prerender(BooleanUtils.isTrue(fetchOptions.prerender))
      .prerenderWaitUntil(fetchOptions.prerenderWaitUntil)
      .prerenderScript(StringUtils.trimToEmpty(fetchOptions.prerenderScript))
      .build()
  }

  fun toDto(fetchOptions: FetchOptions): FetchOptionsDto {
    return FetchOptionsDto.newBuilder()
      .websiteUrl(fetchOptions.websiteUrl)
      .prerender(fetchOptions.prerender)
      .prerenderWaitUntil(toDto(fetchOptions.prerenderWaitUntil))
      .prerenderScript(StringUtils.trimToEmpty(fetchOptions.prerenderScript))
      .build()
  }

  private fun toDto(waitUntil: PuppeteerWaitUntil): PuppeteerWaitUntilDto {
    return when (waitUntil) {
      PuppeteerWaitUntil.load -> PuppeteerWaitUntilDto.load
      PuppeteerWaitUntil.networkidle0 -> PuppeteerWaitUntilDto.networkidle0
      PuppeteerWaitUntil.networkidle2 -> PuppeteerWaitUntilDto.networkidle2
      PuppeteerWaitUntil.domcontentloaded -> PuppeteerWaitUntilDto.domcontentloaded
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
