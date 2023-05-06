package org.migor.rich.rss.api.graphql

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.data.jpa.StandardJpaFields
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.EntityVisibility
import org.migor.rich.rss.data.jpa.enums.NativeFeedStatus
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.BucketEntity
import org.migor.rich.rss.data.jpa.models.FeatureEntity
import org.migor.rich.rss.data.jpa.models.FeatureName
import org.migor.rich.rss.data.jpa.models.FeatureState
import org.migor.rich.rss.data.jpa.models.FeatureValueType
import org.migor.rich.rss.data.jpa.models.GenericFeedEntity
import org.migor.rich.rss.data.jpa.models.ImporterEntity
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.models.PlanAvailability
import org.migor.rich.rss.data.jpa.models.PlanEntity
import org.migor.rich.rss.data.jpa.models.PlanName
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.models.UserSecretEntity
import org.migor.rich.rss.data.jpa.models.UserSecretType
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.migor.rich.rss.generated.types.ArticlesOrderByInput
import org.migor.rich.rss.generated.types.ContentInput
import org.migor.rich.rss.generated.types.Enclosure
import org.migor.rich.rss.generated.types.FeatureBooleanValue
import org.migor.rich.rss.generated.types.FeatureIntValue
import org.migor.rich.rss.generated.types.FeatureValue
import org.migor.rich.rss.generated.types.HarvestEmitType
import org.migor.rich.rss.generated.types.Histogram
import org.migor.rich.rss.generated.types.HistogramFrame
import org.migor.rich.rss.generated.types.HistogramItem
import org.migor.rich.rss.generated.types.OrderByInput
import org.migor.rich.rss.generated.types.Plan
import org.migor.rich.rss.generated.types.PlanSubscription
import org.migor.rich.rss.generated.types.SortOrder
import org.migor.rich.rss.generated.types.User
import org.migor.rich.rss.service.HistogramRawItem
import org.migor.rich.rss.web.PuppeteerEmitType
import org.migor.rich.rss.web.PuppeteerWaitUntil
import org.migor.rich.rss.util.GenericFeedUtil.toDto
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.*
import org.migor.rich.rss.generated.types.Article as ArticleDto
import org.migor.rich.rss.generated.types.ArticleReleaseStatus as ArticleReleaseStatusDto
import org.migor.rich.rss.generated.types.ArticleType as ArticleTypeDto
import org.migor.rich.rss.generated.types.Bucket as BucketDto
import org.migor.rich.rss.generated.types.WebDocument as WebDocumentDto
import org.migor.rich.rss.generated.types.Feature as FeatureDto
import org.migor.rich.rss.generated.types.FeatureName as FeatureNameDto
import org.migor.rich.rss.generated.types.FeatureState as FeatureStateDto
import org.migor.rich.rss.generated.types.GenericFeed as GenericFeedDto
import org.migor.rich.rss.generated.types.GenericFeedSpecification as GenericFeedSpecificationDto
import org.migor.rich.rss.generated.types.Importer as ImporterDto
import org.migor.rich.rss.generated.types.NativeFeed as NativeFeedDto
import org.migor.rich.rss.generated.types.NativeFeedStatus as NativeFeedStatusDto
import org.migor.rich.rss.generated.types.Pagination as PaginationDto
import org.migor.rich.rss.generated.types.PlanAvailability as PlanAvailabilityDto
import org.migor.rich.rss.generated.types.PlanName as PlanNameDto
import org.migor.rich.rss.generated.types.PlanSubscription as PlanSubscriptionDto
import org.migor.rich.rss.generated.types.PuppeteerWaitUntil as PuppeteerWaitUntilDto
import org.migor.rich.rss.generated.types.UserSecret as UserSecretDto
import org.migor.rich.rss.generated.types.UserSecretType as UserSecretTypeDto
import org.migor.rich.rss.generated.types.Visibility as VisibilityDto

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

  fun fromDTO(type: ArticleTypeDto): ArticleType = when (type) {
    ArticleTypeDto.feed -> ArticleType.feed
    ArticleTypeDto.ops -> ArticleType.ops
  }

  fun toDTO(it: ImporterEntity): ImporterDto = ImporterDto.newBuilder()
    .id(it.id.toString())
    .autoRelease(it.autoRelease)
    .filter(it.filter)
    .title(it.title)
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
      val parserOptions = it.feedSpecification.parserOptions
      val fetchOptions = it.feedSpecification.fetchOptions
      val refineOptions = it.feedSpecification.refineOptions
      val selectors = it.feedSpecification.selectors!!
//      val feedUrl = webToFeedTransformer.createFeedUrl(it)

      GenericFeedDto.newBuilder()
        .id(it.id.toString())
//        .nativeFeedId(it.nativeFeedId.toString())
//        FeedUrl=it.feedUrl,
        .specification(
          GenericFeedSpecificationDto.newBuilder()
            .parserOptions(toDto(parserOptions))
            .fetchOptions(toDto(fetchOptions))
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
      .lastUpdatedAt(it.lastUpdatedAt?.time)
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
  }

  fun fromDTO(visibility: VisibilityDto): EntityVisibility = when (visibility) {
    VisibilityDto.isPrivate -> EntityVisibility.isPrivate
    VisibilityDto.isPublic -> EntityVisibility.isPublic
  }

  fun toDTO(visibility: EntityVisibility): VisibilityDto = when (visibility) {
    EntityVisibility.isPrivate -> VisibilityDto.isPrivate
    EntityVisibility.isPublic -> VisibilityDto.isPublic
  }

  fun fromDTO(data: ContentInput): WebDocumentEntity {
    val content = WebDocumentEntity()
    content.url = data.url
    content.title = data.title
    content.contentRaw = data.contentRaw
    content.contentRawMime = data.contentRawMime
    content.contentText = data.contentText
    return content
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

  fun toDTO(it: UserEntity): User =
    User.newBuilder()
      .id(it.id.toString())
      .createdAt(it.createdAt.time)
      .name(it.name)
      .acceptedTermsAndServices(it.hasApprovedTerms)
      .notificationsStreamId(it.notificationsStreamId.toString())
      .build()

  fun toDTO(type: PuppeteerEmitType): HarvestEmitType {
    return when(type) {
      PuppeteerEmitType.pixel -> HarvestEmitType.pixel
      PuppeteerEmitType.text -> HarvestEmitType.text
      PuppeteerEmitType.markup -> HarvestEmitType.markup
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


}
