package org.migor.feedless.api.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.migor.feedless.feed.discovery.RemoteNativeFeedRef
import org.migor.feedless.scrape.GenericFeedRule
import org.migor.feedless.scrape.GenericFeedSelectors
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.toMillis
import com.google.gson.Gson
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import java.time.LocalDateTime
import org.migor.feedless.generated.types.ExtendContentOptions as ExtendContentOptionsDto
import org.migor.feedless.generated.types.FetchActionDebugResponse as FetchActionDebugResponseDto
import org.migor.feedless.generated.types.FetchActionDebugResponseInput as FetchActionDebugResponseInputDto
import org.migor.feedless.generated.types.HttpFetchResponse as HttpFetchResponseDto
import org.migor.feedless.generated.types.HttpFetchResponseInput as HttpFetchResponseInputDto
import org.migor.feedless.generated.types.LogStatement as LogStatementDto
import org.migor.feedless.generated.types.LogStatementInput as LogStatementInputDto
import org.migor.feedless.generated.types.MimeData as MimeDataDto
import org.migor.feedless.generated.types.MimeDataInput as MimeDataInputDto
import org.migor.feedless.generated.types.NetworkRequest as NetworkRequestDto
import org.migor.feedless.generated.types.NetworkRequestInput as NetworkRequestInputDto
import org.migor.feedless.generated.types.RemoteNativeFeed as RemoteNativeFeedDto
import org.migor.feedless.generated.types.ScrapeActionResponse as ScrapeActionResponseDto
import org.migor.feedless.generated.types.ScrapeActionResponseInput as ScrapeActionResponseInputDto
import org.migor.feedless.generated.types.ScrapeExtractFragment as ScrapeExtractFragmentDto
import org.migor.feedless.generated.types.ScrapeExtractFragmentInput as ScrapeExtractFragmentInputDto
import org.migor.feedless.generated.types.ScrapeExtractResponse as ScrapeExtractResponseDto
import org.migor.feedless.generated.types.ScrapeExtractResponseInput as ScrapeExtractResponseInputDto
import org.migor.feedless.generated.types.ScrapeOutputResponse as ScrapeOutputResponseDto
import org.migor.feedless.generated.types.ScrapeOutputResponseInput as ScrapeOutputResponseInputDto
import org.migor.feedless.generated.types.ScrapeResponse as ScrapeResponseDto
import org.migor.feedless.generated.types.ScrapeResponseInput as ScrapeResponseInputDto
import org.migor.feedless.generated.types.Selectors as SelectorsDto
import org.migor.feedless.generated.types.TextData as TextDataDto
import org.migor.feedless.generated.types.TextDataInput as TextDataInputDto
import org.migor.feedless.generated.types.TransientGenericFeed as TransientGenericFeedDto
import org.migor.feedless.generated.types.ViewPort as ViewPortDto
import org.migor.feedless.generated.types.ViewPortInput as ViewPortInputDto

/**
 * MapStruct mapper for scrape-related response objects
 */
@Mapper(config = MapStructConfig::class, uses = [EnumMapper::class])
abstract class ScrapeResponseMapper {

  // Input to Domain conversions
  fun fromDto(input: ScrapeResponseInputDto): ScrapeResponseDto {
    return ScrapeResponseDto(
      errorMessage = input.errorMessage,
      ok = input.ok,
      logs = input.logs.map { fromDto(it) },
      outputs = input.outputs.map { fromDto(it) }
    )
  }

  private fun fromDto(input: LogStatementInputDto): LogStatementDto {
    return LogStatementDto(time = input.time, message = input.message)
  }

  private fun fromDto(input: ScrapeOutputResponseInputDto): ScrapeOutputResponseDto {
    return ScrapeOutputResponseDto(
      index = input.index,
      response = fromDto(input.response)
    )
  }

  private fun fromDto(input: ScrapeActionResponseInputDto): ScrapeActionResponseDto {
    return ScrapeActionResponseDto(
      extract = input.extract?.let { fromDto(it) },
      fetch = input.fetch?.let { fromDto(it) }
    )
  }

  private fun fromDto(input: HttpFetchResponseInputDto): HttpFetchResponseDto {
    return HttpFetchResponseDto(
      data = input.data,
      debug = fromDto(input.debug)
    )
  }

  private fun fromDto(input: FetchActionDebugResponseInputDto): FetchActionDebugResponseDto {
    return FetchActionDebugResponseDto(
      corrId = input.corrId,
      url = input.url,
      screenshot = input.screenshot,
      prerendered = input.prerendered,
      console = input.console,
      network = input.network.map { fromDto(it) },
      cookies = input.cookies,
      statusCode = input.statusCode,
      contentType = input.contentType,
      viewport = fromDto(input.viewport)
    )
  }

  private fun fromDto(input: NetworkRequestInputDto): NetworkRequestDto {
    return NetworkRequestDto(
      url = input.url,
      requestHeaders = input.requestHeaders,
      requestPostData = input.requestPostData,
      responseHeaders = input.responseHeaders,
      responseSize = input.responseSize,
      responseBody = input.responseBody
    )
  }

  private fun fromDto(input: ScrapeExtractResponseInputDto): ScrapeExtractResponseDto {
    return ScrapeExtractResponseDto(
      fragmentName = input.fragmentName,
      fragments = input.fragments.map { fromDto(it) },
    )
  }

  private fun fromDto(input: ScrapeExtractFragmentInputDto): ScrapeExtractFragmentDto {
    return ScrapeExtractFragmentDto(
      data = input.data?.let { fromDto(it) },
      html = input.html?.let { fromDto(it) },
      text = input.text?.let { fromDto(it) },
      uniqueBy = input.uniqueBy,
      extracts = input.extracts?.map { fromDto(it) },
    )
  }

  fun fromDto(input: MimeDataInputDto): MimeDataDto {
    return MimeDataDto(
      mimeType = input.mimeType,
      data = input.data
    )
  }

  fun fromDto(input: TextDataInputDto): TextDataDto {
    return TextDataDto(data = input.data)
  }

  private fun fromDto(input: ViewPortInputDto): ViewPortDto {
    return ViewPortDto(
      height = input.height,
      width = input.width,
      isMobile = input.isMobile,
      isLandscape = input.isLandscape,
    )
  }

  // Domain to DTO conversions
  fun toDto(ref: RemoteNativeFeedRef): RemoteNativeFeedDto {
    return RemoteNativeFeedDto(
      feedUrl = ref.url,
      title = ref.title,
      description = ref.description,
      expired = false,
      items = emptyList(),
      publishedAt = LocalDateTime.now().toMillis()
    )
  }

  fun toDto(rule: GenericFeedRule): TransientGenericFeedDto {
    val selectors = SelectorsDto(
      contextXPath = rule.contextXPath,
      dateXPath = StringUtils.trimToEmpty(rule.dateXPath),
      extendContext = toExtendContentOptions(rule.extendContext),
      linkXPath = rule.linkXPath,
      dateIsStartOfEvent = rule.dateIsStartOfEvent,
      paginationXPath = rule.paginationXPath ?: "",
    )

    return TransientGenericFeedDto(
      count = rule.count,
      hash = CryptUtil.sha1(Gson().toJson(selectors)),
      selectors = selectors,
      score = rule.score,
    )
  }

  private fun toExtendContentOptions(context: org.migor.feedless.scrape.ExtendContext): ExtendContentOptionsDto {
    return when (context) {
      org.migor.feedless.scrape.ExtendContext.PREVIOUS -> ExtendContentOptionsDto.PREVIOUS
      org.migor.feedless.scrape.ExtendContext.NEXT -> ExtendContentOptionsDto.NEXT
      org.migor.feedless.scrape.ExtendContext.NONE -> ExtendContentOptionsDto.NONE
      org.migor.feedless.scrape.ExtendContext.PREVIOUS_AND_NEXT -> ExtendContentOptionsDto.PREVIOUS_AND_NEXT
    }
  }

  fun fromDto(input: SelectorsDto): GenericFeedSelectors {
    return GenericFeedSelectors(
      linkXPath = input.linkXPath,
      extendContext = fromExtendContentOptions(input.extendContext),
      contextXPath = input.contextXPath,
      dateXPath = input.dateXPath,
      dateIsStartOfEvent = BooleanUtils.isTrue(input.dateIsStartOfEvent),
      paginationXPath = input.paginationXPath
    )
  }

  private fun fromExtendContentOptions(options: ExtendContentOptionsDto?): org.migor.feedless.scrape.ExtendContext {
    return when (options) {
      ExtendContentOptionsDto.NEXT -> org.migor.feedless.scrape.ExtendContext.NEXT
      ExtendContentOptionsDto.PREVIOUS -> org.migor.feedless.scrape.ExtendContext.PREVIOUS
      ExtendContentOptionsDto.PREVIOUS_AND_NEXT -> org.migor.feedless.scrape.ExtendContext.PREVIOUS_AND_NEXT
      else -> org.migor.feedless.scrape.ExtendContext.NONE
    }
  }
}


