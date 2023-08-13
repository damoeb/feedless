package org.migor.feedless.api.graphql

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.api.http.WebFragmentType
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ArticleType
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.NativeFeedStatus
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.ArticleEntity
import org.migor.feedless.data.jpa.models.BucketEntity
import org.migor.feedless.data.jpa.models.FeatureEntity
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.models.FeatureState
import org.migor.feedless.data.jpa.models.FeatureValueType
import org.migor.feedless.data.jpa.models.GenericFeedEntity
import org.migor.feedless.data.jpa.models.ImporterEntity
import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.migor.feedless.data.jpa.models.PlanAvailability
import org.migor.feedless.data.jpa.models.PlanEntity
import org.migor.feedless.data.jpa.models.PlanName
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.models.UserSecretEntity
import org.migor.feedless.data.jpa.models.UserSecretType
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.feed.discovery.TransientNativeFeed
import org.migor.feedless.feed.discovery.TransientOrExistingNativeFeed
import org.migor.feedless.generated.types.ArticlesOrderByInput
import org.migor.feedless.generated.types.EmittedScrapeData
import org.migor.feedless.generated.types.EmittedScrapeDataInput
import org.migor.feedless.generated.types.Enclosure
import org.migor.feedless.generated.types.FeatureBooleanValue
import org.migor.feedless.generated.types.FeatureIntValue
import org.migor.feedless.generated.types.FeatureValue
import org.migor.feedless.generated.types.Histogram
import org.migor.feedless.generated.types.HistogramFrame
import org.migor.feedless.generated.types.HistogramItem
import org.migor.feedless.generated.types.NetworkRequest
import org.migor.feedless.generated.types.NetworkRequestInput
import org.migor.feedless.generated.types.OrderByInput
import org.migor.feedless.generated.types.Plan
import org.migor.feedless.generated.types.PlanSubscription
import org.migor.feedless.generated.types.ScrapeDebugResponse
import org.migor.feedless.generated.types.ScrapeDebugResponseInput
import org.migor.feedless.generated.types.ScrapeEmitType
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.ScrapeResponseInput
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.ScrapedElementInput
import org.migor.feedless.generated.types.SortOrder
import org.migor.feedless.generated.types.User
import org.migor.feedless.service.HistogramRawItem
import org.migor.feedless.util.GenericFeedUtil.toDto
import org.migor.feedless.web.PuppeteerEmitType
import org.migor.feedless.web.PuppeteerWaitUntil
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.*
import org.migor.feedless.generated.types.Article as ArticleDto
import org.migor.feedless.generated.types.ArticleReleaseStatus as ArticleReleaseStatusDto
import org.migor.feedless.generated.types.ArticleType as ArticleTypeDto
import org.migor.feedless.generated.types.Bucket as BucketDto
import org.migor.feedless.generated.types.Feature as FeatureDto
import org.migor.feedless.generated.types.FeatureName as FeatureNameDto
import org.migor.feedless.generated.types.FeatureState as FeatureStateDto
import org.migor.feedless.generated.types.GenericFeed as GenericFeedDto
import org.migor.feedless.generated.types.GenericFeedSpecification as GenericFeedSpecificationDto
import org.migor.feedless.generated.types.Importer as ImporterDto
import org.migor.feedless.generated.types.NativeFeed as NativeFeedDto
import org.migor.feedless.generated.types.NativeFeedStatus as NativeFeedStatusDto
import org.migor.feedless.generated.types.Pagination as PaginationDto
import org.migor.feedless.generated.types.PlanAvailability as PlanAvailabilityDto
import org.migor.feedless.generated.types.PlanName as PlanNameDto
import org.migor.feedless.generated.types.PlanSubscription as PlanSubscriptionDto
import org.migor.feedless.generated.types.PuppeteerWaitUntil as PuppeteerWaitUntilDto
import org.migor.feedless.generated.types.TransientNativeFeed as TransientNativeFeedDto
import org.migor.feedless.generated.types.TransientOrExistingNativeFeed as TransientOrExistingNativeFeedDto
import org.migor.feedless.generated.types.UserSecret as UserSecretDto
import org.migor.feedless.generated.types.UserSecretType as UserSecretTypeDto
import org.migor.feedless.generated.types.Visibility as VisibilityDto
import org.migor.feedless.generated.types.WebDocument as WebDocumentDto

object DtoResolver {

  fun toDTO(webDocument: WebDocumentEntity): WebDocumentDto =
    WebDocumentDto.newBuilder()
      .id(webDocument.id.toString())
      .title(webDocument.title!!)
      .imageUrl(webDocument.imageUrl)
      .url(webDocument.url)
      .description(webDocument.description!!)
      .contentTitle(webDocument.contentTitle)
      .contentText(webDocument.contentText)
      .contentRaw(webDocument.contentRaw)
      .contentRawMime(webDocument.contentRawMime)
      .updatedAt(webDocument.updatedAt.time)
      .createdAt(webDocument.createdAt.time)
      .pendingPlugins(webDocument.pendingPlugins)
      .enclosures(webDocument.attachments?.let { it.media.map {
        Enclosure.newBuilder()
          .url(it.url)
          .type(it.format)
          .duration(it.duration)
//          .size(it.duration)
          .build()
      } })
      .tags(getTags(webDocument))
      .publishedAt(webDocument.releasedAt.time)
      .startingAt(webDocument.startingAt?.time)
      .build()

  private fun getTags(content: WebDocumentEntity): List<String> {
    val tags = mutableListOf<String>()
    if (content.hasFulltext) {
      tags.add("fulltext")
    }
    content.startingAt?.let {
      if (it.after(Date())) {
        tags.add("upcoming")
      }
      tags.add("event")
    }
    if (content.hasAudio) {
      tags.add("audio")
    }
    if (content.hasVideo) {
      tags.add("video")
    }
    content.contentText?.let {
      if (it.length <= 140) {
        tags.add("short")
      }
    }
    return tags.distinct()
  }


  fun <T> toPaginatonDTO(page: PageRequest, entities: List<T>): PaginationDto =
    PaginationDto.newBuilder()
      .isEmpty(entities.isEmpty())
      .isFirst(page.offset == 0L)
      .isLast(entities.isEmpty())
      .page(page.pageNumber)
      .pageSize(page.pageSize)
      .build()


  fun toDTO(article: ArticleEntity): ArticleDto =
    ArticleDto.newBuilder()
      .id(article.id.toString())
      .webDocumentId(article.webDocumentId.toString())
      .streamId(article.streamId.toString())
//      .nativeFeedId(article.feedId.toString())
//      Content=toArticleContent(article.content!!),
      .type(toDTO(article.type))
      .status(toDTO(article.status))
      .createdAt(article.createdAt.time)
      .build()

  fun toDTO(status: ReleaseStatus): ArticleReleaseStatusDto = when (status) {
    ReleaseStatus.released -> ArticleReleaseStatusDto.released
    ReleaseStatus.needs_approval -> ArticleReleaseStatusDto.unreleased
    ReleaseStatus.dropped -> ArticleReleaseStatusDto.unreleased
  }
  fun toDTO(status: PlanName): PlanNameDto = when (status) {
    PlanName.free -> PlanNameDto.free
    PlanName.basic -> PlanNameDto.basic
  }

  fun toDTO(av: PlanAvailability): PlanAvailabilityDto = when (av) {
    PlanAvailability.by_request -> PlanAvailabilityDto.by_request
    PlanAvailability.available -> PlanAvailabilityDto.available
    PlanAvailability.unavailable -> PlanAvailabilityDto.unavailable
  }

  private fun toDTO(name: FeatureName): FeatureNameDto {
    return FeatureNameDto.valueOf(name.name)
  }

  fun toDTO(plan: PlanEntity): Plan {
    return Plan.newBuilder()
      .id(plan.id.toString())
      .costs(plan.costs)
      .name(toDTO(plan.name))
      .availability(toDTO(plan.availability))
      .isPrimary(plan.primary)
      .build()
  }

  fun toDTO(state: FeatureState): FeatureStateDto = when (state) {
    FeatureState.off -> FeatureStateDto.off
    FeatureState.beta -> FeatureStateDto.beta
    FeatureState.experimental -> FeatureStateDto.experimental
    FeatureState.stable -> FeatureStateDto.stable
  }

  fun toDTO(type: ArticleType): ArticleTypeDto = when (type) {
    ArticleType.feed -> ArticleTypeDto.feed
    ArticleType.ops -> ArticleTypeDto.ops
  }

  fun fromDTO(status: ArticleReleaseStatusDto) = when (status) {
    ArticleReleaseStatusDto.released -> ReleaseStatus.released
    ArticleReleaseStatusDto.unreleased -> ReleaseStatus.needs_approval
    ArticleReleaseStatusDto.dropped -> ReleaseStatus.dropped
  }

  fun toDTO(it: ImporterEntity): ImporterDto = ImporterDto.newBuilder()
    .id(it.id.toString())
    .autoRelease(it.autoRelease)
    .filter(it.filter)
    .title(it.title)
    .plugins(it.plugins)
    .createdAt(it.createdAt.time)
    .lastUpdatedAt(it.lastUpdatedAt?.time)
    .nativeFeedId(it.feedId.toString())
    .bucketId(it.bucketId.toString())
    .build()

  fun toDTO(bucket: BucketEntity): BucketDto = BucketDto.newBuilder()
    .id(bucket.id.toString())
    .ownerId(bucket.ownerId.toString())
    .title(bucket.title)
    .description(bucket.description)
    .websiteUrl(bucket.websiteUrl)
    .imageUrl(bucket.imageUrl)
    .streamId(bucket.streamId.toString())
    .createdAt(bucket.createdAt.time)
    .tags(bucket.tags?.asList())
    .visibility(toDTO(bucket.visibility))
    .build()

  fun toDTO(feature: FeatureEntity): FeatureDto {
    val value = FeatureValue.newBuilder()
    if(  feature.valueType == FeatureValueType.number) {
      value.numVal(
        FeatureIntValue.newBuilder()
        .value(feature.valueInt!!)
        .build())
    } else {
      value.boolVal(
        FeatureBooleanValue.newBuilder()
        .value(feature.valueBoolean!!)
        .build())
    }

    return FeatureDto.newBuilder()
      .state(toDTO(feature.state))
      .name(toDTO(feature.name))
      .value(value.build())
      .build()
  }

  fun toDTO(it: GenericFeedEntity?): GenericFeedDto? {
    return if (it == null) {
      null
    } else {
      val scrapeOptions = it.feedSpecification.scrapeOptions
      val refineOptions = it.feedSpecification.refineOptions
      val selectors = it.feedSpecification.selectors!!

      GenericFeedDto.newBuilder()
        .id(it.id.toString())
        .specification(
          GenericFeedSpecificationDto.newBuilder()
            .scrapeOptions(scrapeOptions)
            .selectors(toDto(selectors))
            .refineOptions(toDto(refineOptions)).build()
        )
        .createdAt(it.createdAt.time)
        .build()
    }
  }

  fun toDTO(it: NativeFeedEntity): NativeFeedDto =
    NativeFeedDto.newBuilder()
      .id(it.id.toString())
      .title(it.title)
      .description(it.description)
      .imageUrl(it.imageUrl)
      .iconUrl(it.iconUrl)
      .websiteUrl(it.websiteUrl)
      .feedUrl(it.feedUrl)
      .domain(it.domain)
      .status(toDTO(it.status))
      .streamId(it.streamId.toString())
      .genericFeed(toDTO(it.genericFeed))
      .lastCheckedAt(it.lastCheckedAt?.time)
      .errorMessage(it.errorMessage)
      .harvestRateFixed(it.harvestRateFixed)
      .harvestRateMinutes(it.harvestIntervalMinutes)
      .lastChangedAt(it.lastChangedAt?.time)
      .createdAt(it.createdAt.time)
      .lat(it.lat)
      .lon(it.lon)
      .ownerId(it.ownerId.toString())
      .build()

  private fun toDTO(status: NativeFeedStatus): NativeFeedStatusDto = when(status) {
    NativeFeedStatus.SERVICE_UNAVAILABLE -> NativeFeedStatusDto.service_unavailable
    NativeFeedStatus.NOT_FOUND -> NativeFeedStatusDto.not_found
    NativeFeedStatus.OK -> NativeFeedStatusDto.ok
    NativeFeedStatus.DISABLED -> NativeFeedStatusDto.disabled
    NativeFeedStatus.NEVER_FETCHED -> NativeFeedStatusDto.never_fetched
    NativeFeedStatus.DEFECTIVE -> NativeFeedStatusDto.defective
  }

  fun fromDTO(visibility: VisibilityDto?): EntityVisibility = when (visibility) {
    VisibilityDto.isPublic -> EntityVisibility.isPublic
    else -> EntityVisibility.isPrivate
  }

  fun toDTO(visibility: EntityVisibility): VisibilityDto = when (visibility) {
    EntityVisibility.isPrivate -> VisibilityDto.isPrivate
    EntityVisibility.isPublic -> VisibilityDto.isPublic
  }

  fun fromDTO(orderBy: OrderByInput?): Sort {
    val fallback = Sort.by(Sort.Direction.DESC, StandardJpaFields.createdAt)
    return if (orderBy == null) {
      fallback
    } else {
      if (orderBy.title != null) {
        Sort.by(fromDTO(orderBy.title), StandardJpaFields.title)
      } else if (orderBy.createdAt != null) {
        Sort.by(fromDTO(orderBy.createdAt), StandardJpaFields.createdAt)
      } else {
        fallback
      }
    }
  }
  fun fromDTO(orderBy: ArticlesOrderByInput?): Sort {
    val fallback = Sort.by(Sort.Direction.DESC, StandardJpaFields.releasedAt)
    return if (orderBy == null) {
      fallback
    } else {
      if (orderBy.title != null) {
        Sort.by(fromDTO(orderBy.title), StandardJpaFields.title)
      } else if (orderBy.createdAt != null) {
        Sort.by(fromDTO(orderBy.createdAt), StandardJpaFields.releasedAt)
      } else {
        fallback
      }
    }
  }

  private fun fromDTO(sortOrder: SortOrder) = when(sortOrder) {
    SortOrder.asc -> Sort.Direction.ASC
    else -> Sort.Direction.DESC
  }

  fun fromDTO(status: NativeFeedStatusDto) = when(status) {
    NativeFeedStatusDto.ok -> NativeFeedStatus.OK
    NativeFeedStatusDto.disabled -> NativeFeedStatus.DISABLED
    NativeFeedStatusDto.not_found -> NativeFeedStatus.NOT_FOUND
    NativeFeedStatusDto.never_fetched -> NativeFeedStatus.NEVER_FETCHED
    NativeFeedStatusDto.service_unavailable -> NativeFeedStatus.SERVICE_UNAVAILABLE
    NativeFeedStatusDto.defective -> NativeFeedStatus.DEFECTIVE
  }

  fun toDTO(prerenderWaitUntil: PuppeteerWaitUntil): PuppeteerWaitUntilDto = when(prerenderWaitUntil) {
    PuppeteerWaitUntil.load -> PuppeteerWaitUntilDto.load
    PuppeteerWaitUntil.domcontentloaded -> PuppeteerWaitUntilDto.domcontentloaded
    PuppeteerWaitUntil.networkidle0 -> PuppeteerWaitUntilDto.networkidle0
    PuppeteerWaitUntil.networkidle2 -> PuppeteerWaitUntilDto.networkidle2
  }

  fun toDTO(subscription: PlanSubscription): PlanSubscriptionDto {
    return PlanSubscriptionDto.newBuilder()
      .expiry(subscription.expiry)
      .startedAt(subscription.startedAt)
      .planId(subscription.planId)
      .build()
  }

  fun toDTO(it: UserSecretEntity, mask: Boolean = true): UserSecretDto = UserSecretDto.newBuilder()
    .id(it.id.toString())
    .type(toDTO(it.type))
    .value(if (mask) it.value.substring(0..4) + "****" else it.value)
    .valueMasked(mask)
    .validUntil(it.validUntil.time)
    .lastUsed(it.lastUsedAt?.time)
    .build()

  private fun toDTO(type: UserSecretType): UserSecretTypeDto = when(type) {
    UserSecretType.JWT -> UserSecretTypeDto.Jwt
    UserSecretType.SecretKey -> UserSecretTypeDto.SecretKey
  }

  fun fromDto(type: WebFragmentType): ScrapeEmitType = when(type) {
    WebFragmentType.markup -> ScrapeEmitType.markup
    WebFragmentType.text -> ScrapeEmitType.text
    WebFragmentType.pixel -> ScrapeEmitType.pixel
  }

  fun toDTO(it: UserEntity): User =
    User.newBuilder()
      .id(it.id.toString())
      .createdAt(it.createdAt.time)
      .name(it.name)
      .purgeScheduledFor(it.purgeScheduledFor?.time)
      .acceptedTermsAndServices(it.hasApprovedTerms)
      .notificationsStreamId(it.notificationsStreamId.toString())
      .build()

  fun toDTO(type: PuppeteerEmitType): ScrapeEmitType {
    return when(type) {
      PuppeteerEmitType.pixel -> ScrapeEmitType.pixel
      PuppeteerEmitType.text -> ScrapeEmitType.text
      PuppeteerEmitType.markup -> ScrapeEmitType.markup
    }
  }

  fun toDTO(histogramData: List<HistogramRawItem>, frame: HistogramFrame): Histogram {
    return Histogram.newBuilder()
      .frame(frame)
      .items(histogramData.map {
        HistogramItem.newBuilder()
          .index("${it.year}${leftPad(it.month)}${leftPad(it.day)}")
          .count(it.count)
          .build()
      })
      .build()
  }

  private fun leftPad(num: Int): String {
    return StringUtils.leftPad("$num", 2, "0")
  }

  fun toDto(it: TransientOrExistingNativeFeed): TransientOrExistingNativeFeedDto {
    return TransientOrExistingNativeFeedDto.newBuilder()
      ._transient(it.transient?.let { toDTO(it) })
      .existing(it.existing?.let { toDTO(it) })
      .build()
  }

  fun toDto(it: PuppeteerWaitUntil): PuppeteerWaitUntilDto {
    return when(it) {
      PuppeteerWaitUntil.domcontentloaded -> PuppeteerWaitUntilDto.domcontentloaded
      PuppeteerWaitUntil.networkidle2 -> PuppeteerWaitUntilDto.networkidle2
      PuppeteerWaitUntil.networkidle0 -> PuppeteerWaitUntilDto.networkidle0
      PuppeteerWaitUntil.load -> PuppeteerWaitUntilDto.load
    }
  }

  private fun toDTO(it: TransientNativeFeed): TransientNativeFeedDto {
    return TransientNativeFeedDto.newBuilder()
      .description(it.description)
      .title(it.title)
      .url(it.url)
      .type(it.type.name)
      .build()
  }

  fun fromDto(it: ScrapeResponseInput): ScrapeResponse {
    return ScrapeResponse.newBuilder()
      .url(it.url)
      .debug(fromDto(it.debug))
      .errorMessage(it.errorMessage)
      .failed(it.failed)
      .elements(it.elements.map { fromDto(it) })
      .build()
  }

  private fun fromDto(it: ScrapedElementInput): ScrapedElement {
    return ScrapedElement.newBuilder()
      .xpath(it.xpath)
      .data(it.data.map { fromDto(it) })
      .build()
  }

  private fun fromDto(it: EmittedScrapeDataInput): EmittedScrapeData {
    return EmittedScrapeData.newBuilder()
      .type(it.type)
      .markup(it.markup)
      .text(it.text)
      .pixel(it.pixel)
      .build()
  }

  private fun fromDto(it: ScrapeDebugResponseInput): ScrapeDebugResponse {
    return ScrapeDebugResponse.newBuilder()
      .corrId(it.corrId)
      .console(it.console)
      .cookies(it.cookies)
      .network(it.network.map { fromDto(it) })
      .body(it.body)
      .contentType(it.contentType)
      .statusCode(it.statusCode)
      .screenshot(it.screenshot)
      .build()
  }

  private fun fromDto(it: NetworkRequestInput): NetworkRequest {
    return NetworkRequest.newBuilder()
      .responseBody(it.responseBody)
      .responseHeaders(it.responseHeaders)
      .responseSize(it.responseSize)
      .requestHeaders(it.requestHeaders)
      .requestPostData(it.requestPostData)
      .build()
  }
}
