package org.migor.rich.rss.util

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.generated.types.ExtendContentOptions
import org.migor.rich.rss.generated.types.FetchOptions
import org.migor.rich.rss.generated.types.FetchOptionsInput
import org.migor.rich.rss.generated.types.GenericFeedSpecificationInput
import org.migor.rich.rss.generated.types.ParserOptions
import org.migor.rich.rss.generated.types.ParserOptionsInput
import org.migor.rich.rss.generated.types.RefineOptions
import org.migor.rich.rss.generated.types.RefineOptionsInput
import org.migor.rich.rss.generated.types.Selectors
import org.migor.rich.rss.generated.types.SelectorsInput
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.transform.ExtendContext
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import org.migor.rich.rss.transform.GenericFeedParserOptions
import org.migor.rich.rss.transform.GenericFeedRefineOptions
import org.migor.rich.rss.transform.GenericFeedSelectors
import org.migor.rich.rss.transform.GenericFeedSpecification
import org.migor.rich.rss.transform.PuppeteerWaitUntil
import org.migor.rich.rss.generated.types.ArticleRecoveryType as ArticleRecoveryTypeDto
import org.migor.rich.rss.generated.types.PuppeteerWaitUntil as PuppeteerWaitUntilDto

object GenericFeedUtil {
  private fun fromDto(selectors: SelectorsInput): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = selectors.linkXPath,
      extendContext = fromDto(selectors.extendContext),
      contextXPath = selectors.contextXPath,
      dateXPath = selectors.dateXPath,
      dateIsStartOfEvent = selectors.dateIsStartOfEvent
    )
  }

  private fun fromDto(extendContext: ExtendContentOptions): ExtendContext {
    return when (extendContext) {
      ExtendContentOptions.NEXT -> ExtendContext.NEXT
      ExtendContentOptions.PREVIOUS -> ExtendContext.PREVIOUS
      ExtendContentOptions.NONE -> ExtendContext.NONE
      else -> throw RuntimeException("ExtendContentOptionsDto $extendContext is not supported")
    }
  }

  fun fromDto(selectors: Selectors): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = selectors.linkXPath,
      extendContext = fromDto(selectors.extendContext),
      contextXPath = selectors.contextXPath,
      dateXPath = selectors.dateXPath,
      dateIsStartOfEvent = selectors.dateIsStartOfEvent
    )
  }

  private fun fromDto(recovery: ArticleRecoveryTypeDto): ArticleRecoveryType {
    return when (recovery) {
      ArticleRecoveryTypeDto.METADATA -> ArticleRecoveryType.METADATA
      ArticleRecoveryTypeDto.FULL -> ArticleRecoveryType.FULL
      ArticleRecoveryTypeDto.NONE -> ArticleRecoveryType.NONE
//      else -> throw RuntimeException("ArticleRecoveryTypeDto $recovery is not supported")
    }
  }

//  fun fromDto(specification: GenericFeedSpecificationDto): GenericFeedSpecification {
//    return GenericFeedSpecification(
//      .selectors(fromDto(specification.selectors))
//      .parserOptions(fromDto(specification.parserOptions))
//      .fetchOptions(fromDto(specification.fetchOptions))
//      .refineOptions(fromDto(specification.refineOptions))
//    )
//  }

  fun fromDto(specification: GenericFeedSpecificationInput): GenericFeedSpecification {
    return GenericFeedSpecification(
      selectors = fromDto(specification.selectors),
      parserOptions = fromDto(specification.parserOptions),
      fetchOptions = fromDto(specification.fetchOptions),
      refineOptions = fromDto(specification.refineOptions)
    )

  }

  fun fromDto(refineOptions: RefineOptions): GenericFeedRefineOptions {
    return GenericFeedRefineOptions(
      filter = StringUtils.trimToEmpty(refineOptions.filter),
      recovery = fromDto(refineOptions.recovery)
    )
  }

  private fun fromDto(refineOptions: RefineOptionsInput): GenericFeedRefineOptions {
    return GenericFeedRefineOptions(
      filter = StringUtils.trimToEmpty(refineOptions.filter),
      recovery = fromDto(refineOptions.recovery)
    )
  }

  fun fromDto(fetchOptions: FetchOptions): GenericFeedFetchOptions {
    return GenericFeedFetchOptions(
      websiteUrl = fetchOptions.websiteUrl,
      prerender = fetchOptions.prerender,
      prerenderWaitUntil = fromDto(fetchOptions.prerenderWaitUntil),
      prerenderWithoutMedia = fetchOptions.prerenderWithoutMedia,
      prerenderScript = StringUtils.trimToEmpty(fetchOptions.prerenderScript)
    )
  }

  fun fromDto(fetchOptions: FetchOptionsInput): GenericFeedFetchOptions {
    return GenericFeedFetchOptions(
      websiteUrl = fetchOptions.websiteUrl,
      prerender = fetchOptions.prerender,
      prerenderWaitUntil = fromDto(fetchOptions.prerenderWaitUntil),
      prerenderWithoutMedia = fetchOptions.prerenderWithoutMedia,
      prerenderScript = StringUtils.trimToEmpty(fetchOptions.prerenderScript)
    )
  }

  private fun fromDto(waitUntil: PuppeteerWaitUntilDto): PuppeteerWaitUntil {
    return when (waitUntil) {
      PuppeteerWaitUntilDto.domcontentloaded -> PuppeteerWaitUntil.domcontentloaded
      PuppeteerWaitUntilDto.networkidle0 -> PuppeteerWaitUntil.networkidle0
      PuppeteerWaitUntilDto.networkidle2 -> PuppeteerWaitUntil.networkidle2
      PuppeteerWaitUntilDto.load -> PuppeteerWaitUntil.load
//      else -> throw IllegalArgumentException("PuppeteerWaitUntilDto $waitUntil not supported")
    }
  }

  fun fromDto(parserOptions: ParserOptions) = GenericFeedParserOptions(
    strictMode = parserOptions.strictMode,
    version = ""
  )

  private fun fromDto(parserOptions: ParserOptionsInput) = GenericFeedParserOptions(
    strictMode = parserOptions.strictMode,
    version = ""
  )


  fun toDto(parserOptions: ParserOptionsInput) = ParserOptions
    .newBuilder()
    .strictMode(parserOptions.strictMode)
    .build()

  fun toDto(fetchOptions: FetchOptionsInput): FetchOptions {
    return FetchOptions.newBuilder()
      .websiteUrl(fetchOptions.websiteUrl)
      .prerender(BooleanUtils.isTrue(fetchOptions.prerender))
      .prerenderWaitUntil(fetchOptions.prerenderWaitUntil)
      .prerenderWithoutMedia(fetchOptions.prerenderWithoutMedia)
      .prerenderScript(StringUtils.trimToEmpty(fetchOptions.prerenderScript))
      .build()
  }

  fun toDto(recovery: ArticleRecoveryType): ArticleRecoveryTypeDto {
    return when (recovery) {
      ArticleRecoveryType.FULL -> ArticleRecoveryTypeDto.FULL
      ArticleRecoveryType.METADATA -> ArticleRecoveryTypeDto.METADATA
      else -> ArticleRecoveryTypeDto.NONE
    }
  }

  fun toDto(parserOptions: GenericFeedParserOptions) =
    ParserOptions.newBuilder().strictMode(parserOptions.strictMode).build()

  fun toDto(fetchOptions: GenericFeedFetchOptions): FetchOptions {
    return FetchOptions.newBuilder()
      .websiteUrl(fetchOptions.websiteUrl)
      .prerender(fetchOptions.prerender)
      .prerenderWithoutMedia(fetchOptions.prerenderWithoutMedia)
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
//      else -> throw RuntimeException("PuppeteerWaitUntil $waitUntil is not supported")
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
//      else -> throw RuntimeException("ExtendContext $extendContext is not supported")
    }
  }

  fun toDto(refineOptions: GenericFeedRefineOptions): RefineOptions {
    return RefineOptions.newBuilder()
      .filter(refineOptions.filter)
      .recovery(toDto(refineOptions.recovery))
      .build()
  }
}
