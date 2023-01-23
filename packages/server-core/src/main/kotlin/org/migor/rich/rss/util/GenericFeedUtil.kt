package org.migor.rich.rss.util

import org.migor.rich.rss.generated.ArticleRecoveryTypeDto
import org.migor.rich.rss.generated.ExtendContentOptionsDto
import org.migor.rich.rss.generated.FetchOptionsDto
import org.migor.rich.rss.generated.FetchOptionsInputDto
import org.migor.rich.rss.generated.GenericFeedSpecificationDto
import org.migor.rich.rss.generated.GenericFeedSpecificationInputDto
import org.migor.rich.rss.generated.ParserOptionsDto
import org.migor.rich.rss.generated.ParserOptionsInputDto
import org.migor.rich.rss.generated.PuppeteerWaitUntilDto
import org.migor.rich.rss.generated.RefineOptionsDto
import org.migor.rich.rss.generated.RefineOptionsInputDto
import org.migor.rich.rss.generated.SelectorsDto
import org.migor.rich.rss.generated.SelectorsInputDto
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.transform.ExtendContext
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import org.migor.rich.rss.transform.GenericFeedParserOptions
import org.migor.rich.rss.transform.GenericFeedRefineOptions
import org.migor.rich.rss.transform.GenericFeedSelectors
import org.migor.rich.rss.transform.GenericFeedSpecification
import org.migor.rich.rss.transform.PuppeteerWaitUntil

object GenericFeedUtil {
  fun fromDto(selectors: SelectorsInputDto): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = selectors.linkXPath,
      extendContext = fromDto(selectors.extendContext),
      contextXPath = selectors.contextXPath,
      dateXPath = selectors.dateXPath,
    )
  }

  private fun fromDto(extendContext: ExtendContentOptionsDto): ExtendContext {
    return when(extendContext) {
      ExtendContentOptionsDto.NEXT -> ExtendContext.NEXT
      ExtendContentOptionsDto.PREVIOUS -> ExtendContext.PREVIOUS
      ExtendContentOptionsDto.NONE -> ExtendContext.NONE
      else -> throw RuntimeException("ExtendContentOptionsDto $extendContext is not supported")
    }
  }

  fun fromDto(selectors: SelectorsDto): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = selectors.linkXPath,
      extendContext = fromDto(selectors.extendContext),
      contextXPath = selectors.contextXPath,
      dateXPath = selectors.dateXPath,
    )
  }

  private fun fromDto(recovery: ArticleRecoveryTypeDto): ArticleRecoveryType {
    return when(recovery) {
      ArticleRecoveryTypeDto.METADATA -> ArticleRecoveryType.METADATA
      ArticleRecoveryTypeDto.FULL -> ArticleRecoveryType.FULL
      ArticleRecoveryTypeDto.NONE -> ArticleRecoveryType.NONE
      else -> throw RuntimeException("ArticleRecoveryTypeDto $recovery is not supported")
    }
  }

  fun fromDto(specification: GenericFeedSpecificationDto): GenericFeedSpecification {
    return GenericFeedSpecification(
      selectors = fromDto(specification.selectors),
      parserOptions = fromDto(specification.parserOptions),
      fetchOptions = fromDto(specification.fetchOptions),
      refineOptions = fromDto(specification.refineOptions),
    )
  }

  fun fromDto(specification: GenericFeedSpecificationInputDto): GenericFeedSpecification {
    return GenericFeedSpecification(
      selectors = fromDto(specification.selectors),
      parserOptions = fromDto(specification.parserOptions),
      fetchOptions = fromDto(specification.fetchOptions),
      refineOptions = fromDto(specification.refineOptions),
    )

  }

  fun fromDto(refineOptions: RefineOptionsDto): GenericFeedRefineOptions {
    return GenericFeedRefineOptions(
      filter = refineOptions.filter,
      recovery = fromDto(refineOptions.recovery),
    )
  }

  private fun fromDto(refineOptions: RefineOptionsInputDto): GenericFeedRefineOptions {
    return GenericFeedRefineOptions(
      filter = refineOptions.filter,
      recovery = fromDto(refineOptions.recovery),
    )
  }

  fun fromDto(fetchOptions: FetchOptionsDto): GenericFeedFetchOptions {
    return GenericFeedFetchOptions(
      websiteUrl = fetchOptions.websiteUrl,
      prerender = fetchOptions.prerender,
      prerenderWaitUntil = fromDto(fetchOptions.prerenderWaitUntil),
      prerenderWithoutMedia = fetchOptions.prerenderWithoutMedia,
      prerenderScript = fetchOptions.prerenderScript
    )
  }

  fun fromDto(fetchOptions: FetchOptionsInputDto): GenericFeedFetchOptions {
    return GenericFeedFetchOptions(
      websiteUrl = fetchOptions.websiteUrl,
      prerender = fetchOptions.prerender,
      prerenderWaitUntil = fromDto(fetchOptions.prerenderWaitUntil),
      prerenderWithoutMedia = fetchOptions.prerenderWithoutMedia,
      prerenderScript = fetchOptions.prerenderScript
    )
  }

  private fun fromDto(waitUntil: PuppeteerWaitUntilDto): PuppeteerWaitUntil {
    return when(waitUntil) {
      PuppeteerWaitUntilDto.domcontentloaded -> PuppeteerWaitUntil.domcontentloaded
      PuppeteerWaitUntilDto.networkidle0 -> PuppeteerWaitUntil.networkidle0
      PuppeteerWaitUntilDto.networkidle2 -> PuppeteerWaitUntil.networkidle2
      PuppeteerWaitUntilDto.load -> PuppeteerWaitUntil.load
      else -> throw IllegalArgumentException("PuppeteerWaitUntilDto $waitUntil not supported")
    }
  }

  fun fromDto(parserOptions: ParserOptionsDto): GenericFeedParserOptions {
    return GenericFeedParserOptions(
      strictMode = parserOptions.strictMode,
      eventFeed = parserOptions.eventFeed,
      version = "",
    )
  }

  private fun fromDto(parserOptions: ParserOptionsInputDto): GenericFeedParserOptions {
    return GenericFeedParserOptions(
      strictMode = parserOptions.strictMode,
      eventFeed = parserOptions.eventFeed,
      version = "",
    )
  }

  fun toDto(parserOptions: ParserOptionsInputDto): ParserOptionsDto {
    return ParserOptionsDto.builder()
      .setEventFeed(parserOptions.eventFeed)
      .setStrictMode(parserOptions.strictMode)
      .build()
  }

  fun toDto(fetchOptions: FetchOptionsInputDto): FetchOptionsDto {
    return FetchOptionsDto.builder()
      .setPrerender(fetchOptions.prerender)
      .setPrerenderWaitUntil(fetchOptions.prerenderWaitUntil)
      .setPrerenderScript(fetchOptions.prerenderScript)
      .setPrerenderWithoutMedia(fetchOptions.prerenderWithoutMedia)
      .setWebsiteUrl(fetchOptions.websiteUrl)
      .build()
  }

  fun toDto(recovery: ArticleRecoveryType): ArticleRecoveryTypeDto {
    return when(recovery) {
      ArticleRecoveryType.FULL -> ArticleRecoveryTypeDto.FULL
      ArticleRecoveryType.METADATA -> ArticleRecoveryTypeDto.METADATA
      else -> ArticleRecoveryTypeDto.NONE
    }
  }

  fun toDto(parserOptions: GenericFeedParserOptions): ParserOptionsDto {
    return ParserOptionsDto.builder()
      .setEventFeed(parserOptions.eventFeed)
      .setStrictMode(parserOptions.strictMode)
      .build()
  }

  fun toDto(fetchOptions: GenericFeedFetchOptions): FetchOptionsDto {
    return FetchOptionsDto.builder()
      .setPrerenderScript(fetchOptions.prerenderScript)
      .setPrerender(fetchOptions.prerender)
      .setPrerenderWithoutMedia(fetchOptions.prerenderWithoutMedia)
      .setPrerenderWaitUntil(toDto(fetchOptions.prerenderWaitUntil))
      .setWebsiteUrl(fetchOptions.websiteUrl)
      .build()
  }

  private fun toDto(waitUntil: PuppeteerWaitUntil): PuppeteerWaitUntilDto {
    return when(waitUntil) {
      PuppeteerWaitUntil.load -> PuppeteerWaitUntilDto.load
      PuppeteerWaitUntil.networkidle0 -> PuppeteerWaitUntilDto.networkidle0
      PuppeteerWaitUntil.networkidle2 -> PuppeteerWaitUntilDto.networkidle2
      PuppeteerWaitUntil.domcontentloaded -> PuppeteerWaitUntilDto.domcontentloaded
      else -> throw RuntimeException("PuppeteerWaitUntil $waitUntil is not supported")
    }
  }

  fun toDto(selectors: GenericFeedSelectors): SelectorsDto {
    return SelectorsDto.builder()
      .setContextXPath(selectors.contextXPath)
      .setDateXPath(selectors.dateXPath)
      .setLinkXPath(selectors.linkXPath)
      .setExtendContext(toDto(selectors.extendContext))
      .build()
  }

  fun toDto(extendContext: ExtendContext): ExtendContentOptionsDto {
    return when(extendContext) {
      ExtendContext.PREVIOUS -> ExtendContentOptionsDto.PREVIOUS
      ExtendContext.NEXT -> ExtendContentOptionsDto.NEXT
      ExtendContext.NONE -> ExtendContentOptionsDto.NONE
      ExtendContext.PREVIOUS_AND_NEXT -> ExtendContentOptionsDto.PREVIOUS_AND_NEXT
      else -> throw RuntimeException("ExtendContext $extendContext is not supported")
    }
  }

  fun toDto(refineOptions: GenericFeedRefineOptions): RefineOptionsDto {
    return RefineOptionsDto.builder()
      .setFilter(refineOptions.filter)
      .setRecovery(toDto(refineOptions.recovery))
      .build()
  }
}
