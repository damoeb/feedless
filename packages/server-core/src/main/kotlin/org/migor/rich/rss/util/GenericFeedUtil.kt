package org.migor.rich.rss.util

import org.migor.rich.rss.generated.ArticleRecoveryTypeDto
import org.migor.rich.rss.generated.FetchOptionsDto
import org.migor.rich.rss.generated.FetchOptionsInputDto
import org.migor.rich.rss.generated.GenericFeedSpecificationDto
import org.migor.rich.rss.generated.GenericFeedSpecificationInputDto
import org.migor.rich.rss.generated.ParserOptionsDto
import org.migor.rich.rss.generated.ParserOptionsInputDto
import org.migor.rich.rss.generated.RefineOptionsDto
import org.migor.rich.rss.generated.RefineOptionsInputDto
import org.migor.rich.rss.generated.SelectorsDto
import org.migor.rich.rss.generated.SelectorsInputDto
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import org.migor.rich.rss.transform.GenericFeedParserOptions
import org.migor.rich.rss.transform.GenericFeedRefineOptions
import org.migor.rich.rss.transform.GenericFeedSelectors
import org.migor.rich.rss.transform.GenericFeedSpecification

object GenericFeedUtil {
  fun fromDto(selectors: SelectorsInputDto): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = selectors.linkXPath,
      extendContext = selectors.extendContext,
      contextXPath = selectors.contextXPath,
      dateXPath = selectors.dateXPath,
    )
  }

  private fun fromDto(selectors: SelectorsDto): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = selectors.linkXPath,
      extendContext = selectors.extendContext,
      contextXPath = selectors.contextXPath,
      dateXPath = selectors.dateXPath,
    )
  }

  private fun fromDto(recovery: ArticleRecoveryTypeDto): ArticleRecoveryType {
    return ArticleRecoveryType.NONE
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

  private fun fromDto(refineOptions: RefineOptionsDto): GenericFeedRefineOptions {
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

  private fun fromDto(fetchOptions: FetchOptionsDto): GenericFeedFetchOptions {
    return GenericFeedFetchOptions(
      websiteUrl = fetchOptions.websiteUrl,
      prerender = fetchOptions.prerender,
      prerenderDelayMs = fetchOptions.prerenderDelayMs,
      prerenderWithoutMedia = fetchOptions.prerenderWithoutMedia,
      prerenderScript = fetchOptions.prerenderScript
    )
  }

  fun fromDto(fetchOptions: FetchOptionsInputDto): GenericFeedFetchOptions {
    return GenericFeedFetchOptions(
      websiteUrl = fetchOptions.websiteUrl,
      prerender = fetchOptions.prerender,
      prerenderDelayMs = fetchOptions.prerenderDelayMs,
      prerenderWithoutMedia = fetchOptions.prerenderWithoutMedia,
      prerenderScript = fetchOptions.prerenderScript
    )
  }

  private fun fromDto(parserOptions: ParserOptionsDto): GenericFeedParserOptions {
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
      .setPrerenderDelayMs(fetchOptions.prerenderDelayMs)
      .setPrerenderScript(fetchOptions.prerenderScript)
      .setPrerenderWithoutMedia(fetchOptions.prerenderWithoutMedia)
      .setWebsiteUrl(fetchOptions.websiteUrl)
      .build()
  }

}
