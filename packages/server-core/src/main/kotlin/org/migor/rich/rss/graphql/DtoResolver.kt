package org.migor.rich.rss.graphql

import org.migor.rich.rss.data.jpa.StandardJpaFields
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.EntityVisibility
import org.migor.rich.rss.data.jpa.enums.NativeFeedStatus
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.BucketEntity
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.FeatureName
import org.migor.rich.rss.data.jpa.models.FeatureState
import org.migor.rich.rss.data.jpa.models.FeatureValueType
import org.migor.rich.rss.data.jpa.models.GenericFeedEntity
import org.migor.rich.rss.data.jpa.models.ImporterEntity
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.models.PlanAvailability
import org.migor.rich.rss.data.jpa.models.PlanName
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.migor.rich.rss.generated.types.ArticlesOrderByInput
import org.migor.rich.rss.generated.types.ContentInput
import org.migor.rich.rss.generated.types.Feature
import org.migor.rich.rss.generated.types.FeatureBooleanValue
import org.migor.rich.rss.generated.types.FeatureIntValue
import org.migor.rich.rss.generated.types.FeatureValue
import org.migor.rich.rss.generated.types.OrderByInput
import org.migor.rich.rss.generated.types.SortOrder
import org.migor.rich.rss.service.PlanFeature
import org.migor.rich.rss.util.GenericFeedUtil
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.*
import org.migor.rich.rss.generated.types.Article as ArticleDto
import org.migor.rich.rss.generated.types.ArticleReleaseStatus as ArticleReleaseStatusDto
import org.migor.rich.rss.generated.types.ArticleType as ArticleTypeDto
import org.migor.rich.rss.generated.types.Bucket as BucketDto
import org.migor.rich.rss.generated.types.Content as ContentDto
import org.migor.rich.rss.generated.types.FeatureName as FeatureNameDto
import org.migor.rich.rss.generated.types.FeatureState as FeatureStateDto
import org.migor.rich.rss.generated.types.GenericFeed as GenericFeedDto
import org.migor.rich.rss.generated.types.GenericFeedSpecification as GenericFeedSpecificationDto
import org.migor.rich.rss.generated.types.Importer as ImporterDto
import org.migor.rich.rss.generated.types.NativeFeed as NativeFeedDto
import org.migor.rich.rss.generated.types.Pagination as PaginationDto
import org.migor.rich.rss.generated.types.PlanAvailability as PlanAvailabilityDto
import org.migor.rich.rss.generated.types.PlanName as PlanNameDto
import org.migor.rich.rss.generated.types.Visibility as VisibilityDto
import org.migor.rich.rss.generated.types.WebDocument as WebDocumentDto
import org.migor.rich.rss.generated.types.NativeFeedStatus as NativeFeedStatusDto

object DtoResolver {

  fun toDTO(content: ContentEntity): ContentDto =
    ContentDto.newBuilder()
      .id(content.id.toString())
      .title(content.title!!)
      .imageUrl(content.imageUrl)
      .url(content.url)
      .description(content.description!!)
      .contentTitle(content.contentTitle)
      .contentText(content.contentText)
      .contentRaw(content.contentRaw)
      .contentRawMime(content.contentRawMime)
      .updatedAt(content.updatedAt.time)
      .createdAt(content.createdAt.time)
//      .hasFulltext(content.hasFulltext)
      .tags(getTags(content))
      .publishedAt(content.publishedAt.time)
      .startingAt(content.startingAt?.time)
      .build()

  private fun getTags(content: ContentEntity): List<String> {
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
      .build()


  fun toDTO(article: ArticleEntity): ArticleDto =
    ArticleDto.newBuilder()
      .id(article.id.toString())
      .contentId(article.contentId.toString())
      .streamId(article.streamId.toString())
      .nativeFeedId(article.feedId.toString())
//      Content=toArticleContent(article.content!!),
      .type(toDTO(article.type))
      .status(toDTO(article.status))
      .createdAt(article.createdAt.time)
      .build()

  fun toDTO(d: WebDocumentEntity): WebDocumentDto =
    WebDocumentDto.newBuilder()
      .id(d.id.toString())
      .type(d.type!!)
      .url(d.url!!)
      .title(d.title!!)
      .description(d.description)
      .score(d.score)
      .imageUrl(d.imageUrl)
      .createdAt(d.createdAt.time)
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

  fun toDTO(feature: PlanFeature): Feature {
    val value = FeatureValue.newBuilder()
    if( feature.valueType == FeatureValueType.number) {
      value.numVal(FeatureIntValue.newBuilder()
        .value(feature.value as Int)
        .build())
    } else {
      value.boolVal(
        FeatureBooleanValue.newBuilder()
        .value(feature.value as Boolean)
        .build())
    }

    return Feature.newBuilder()
      .state(toDTO(feature.state))
      .name(toDTO(feature.name))
      .value(value.build())
      .build()
  }

  private fun toDTO(name: FeatureName): FeatureNameDto {
    return FeatureNameDto.valueOf(name.name)
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
    .email(it.emailForward)
    .webhook(it.webhookUrl)
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
    .tags(bucket.tags)
    .visibility(toDTO(bucket.visibility))
    .build()


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
        .nativeFeedId(it.nativeFeedId.toString())
//        FeedUrl=it.feedUrl,
        .specification(
          GenericFeedSpecificationDto.newBuilder()
            .parserOptions(GenericFeedUtil.toDto(parserOptions))
            .fetchOptions(GenericFeedUtil.toDto(fetchOptions))
            .selectors(GenericFeedUtil.toDto(selectors))
            .refineOptions(GenericFeedUtil.toDto(refineOptions)).build()
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

  fun fromDTO(data: ContentInput): ContentEntity {
    val content = ContentEntity()
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

}
