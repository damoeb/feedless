package org.migor.rich.rss.graphql

import graphql.kickstart.tools.GraphQLQueryResolver
import org.apache.commons.lang3.BooleanUtils
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.GenericFeedEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.ArticleContentGql
import org.migor.rich.rss.generated.ArticleGql
import org.migor.rich.rss.generated.ArticleInStreamGql
import org.migor.rich.rss.generated.ArticleTypeGql
import org.migor.rich.rss.generated.ArticlesByStreamIdFilterInputGql
import org.migor.rich.rss.generated.ArticlesByStreamIdResponseGql
import org.migor.rich.rss.generated.BucketByIdInputGql
import org.migor.rich.rss.generated.BucketGql
import org.migor.rich.rss.generated.BucketResponseGql
import org.migor.rich.rss.generated.DiscoverFeedsInputGql
import org.migor.rich.rss.generated.EnclosureGql
import org.migor.rich.rss.generated.FeedDiscoveryResponseGql
import org.migor.rich.rss.generated.GenericFeedGql
import org.migor.rich.rss.generated.GenericFeedRuleGql
import org.migor.rich.rss.generated.ImporterGql
import org.migor.rich.rss.generated.NativeFeedGql
import org.migor.rich.rss.generated.NativeFeedReferenceGql
import org.migor.rich.rss.generated.PaginationGql
import org.migor.rich.rss.generated.ReleaseStatusGql
import org.migor.rich.rss.generated.SearchBucketInputGql
import org.migor.rich.rss.generated.SearchBucketMatchGql
import org.migor.rich.rss.generated.SearchBucketResponseGql
import org.migor.rich.rss.generated.SearchFeedInputGql
import org.migor.rich.rss.generated.SearchFeedMatchGql
import org.migor.rich.rss.generated.SearchFeedResponseGql
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.JsonUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class GraphqlQuery : GraphQLQueryResolver {

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var feedDiscovery: FeedDiscoveryService

  @Transactional(propagation = Propagation.REQUIRED)
  fun bucketById(data: BucketByIdInputGql): BucketResponseGql {
    val bucket = bucketService.findByBucketId(data.id)
    return BucketResponseGql.builder()
      .setBucket(BucketGql.builder()
        .setName(bucket.name)
        .setDescription(bucket.description)
        .setId(bucket.id.toString())
        .setWebsiteUrl(bucket.websiteUrl)
        .setStreamId(bucket.streamId.toString())
        .setCreatedAt(bucket.createdAt.time)
        .setImporters(bucket.importers.map { ImporterGql.builder()
          .setId(it.id.toString())
          .setAutoRelease(it.autoRelease)
          .setFeedId(it.feedId.toString())
          .setFeed(toNativeFeedGql(it.feed!!))
          .setBucketId(it.bucketId.toString())
          .setCreatedAt(it.createdAt.time)
          .build()})
        .build())
      .build()
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun searchBucket(data: SearchBucketInputGql): SearchBucketResponseGql? {
    if(data.anywhere != null ) {
      val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
      val page = bucketService.findAllMatching(data.anywhere.query, pageable)

      return SearchBucketResponseGql.builder()
        .setPagination(toPagination(page))
        .setMatches(page.toList().map { SearchBucketMatchGql.builder()
          .setId(it.id.toString())
          .setTitle(it.name)
          .setSubtitle(it.description)
          .setUrl("/bucket/${it.id}")
//          .setScore(0)
          .setCreatedAt(it.createdAt.time)
          .setLastUpdatedAt(it.lastUpdatedAt?.time)
          .build() })
        .build()
    }
    return null
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun searchFeed(data: SearchFeedInputGql): SearchFeedResponseGql? {
      val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
      val page = feedService.findAllMatching(data.query, pageable)

      return SearchFeedResponseGql.builder()
        .setPagination(toPagination(page))
        .setMatches(page.toList().map { SearchFeedMatchGql.builder()
          .setId(it.id.toString())
          .setTitle(it.title)
          .setSubtitle(it.description)
          .setUrl(it.feedUrl)
          .setCreatedAt(it.createdAt.time)
          .setLastUpdatedAt(it.lastUpdatedAt?.time)
          .build() })
        .build()
  }

  fun discoverFeeds(data: DiscoverFeedsInputGql): FeedDiscoveryResponseGql {
    val corrId = handleCorrId(data.corrId)
    val discovery = feedDiscovery.discoverFeeds(corrId, data.url, null, BooleanUtils.isTrue(data.prerender), false)
    val response = discovery.results
    return FeedDiscoveryResponseGql.builder()
      .setFailed(response.failed)
      .setMimeType(response.mimeType)
      .setErrorMessage(response.errorMessage)
      .setGenericFeedRules(response.genericFeedRules.map { GenericFeedRuleGql.builder()
        .setFeedUrl(it.feedUrl)
        .setCount(it.count)
        .setContextXPath(it.contextXPath)
        .setDateXPath(it.dateXPath)
        .setExtendContext(it.extendContext)
        .setLinkXPath(it.linkXPath)
        .setScore(it.score)
        .build()
      })
      .setNativeFeeds(response.nativeFeeds.map { NativeFeedReferenceGql.builder()
        .setUrl(it.url)
        .setTitle(it.title)
        .setType(it.type!!.name)
        .setDescription(it.description)
        .build()
      })
      .build()
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun article(id: String): ArticleGql? {
    return articleService.findById(UUID.fromString(id)).map { toArticle(it) }.orElseThrow()
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun articleContent(id: String): ArticleContentGql? {
    return articleService.findContentById(UUID.fromString(id)).map { toArticleContent(it) }.orElseThrow()
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun nativeFeed(id: String): NativeFeedGql? {
    return feedService.findNativeById(UUID.fromString(id)).map { toNativeFeedGql(it) }.orElseThrow()
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun genericFeed(id: String): GenericFeedGql? {
    return feedService.findGenericById(UUID.fromString(id)).map { toGenericFeedGql(it) }.orElseThrow()
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun articlesByStreamId(filter: ArticlesByStreamIdFilterInputGql): ArticlesByStreamIdResponseGql {
    val streamId = UUID.fromString(filter.streamId)
    val page = 0
    val type = filter.type
    val status = filter.status
    val result = articleService.findAllByStreamId2(streamId, page, fromDto(type), fromDto(status))

    return ArticlesByStreamIdResponseGql.builder()
      .setPagination(toPagination(result))
      .setArticles(result.toList().map { ArticleInStreamGql.builder()
        .setFeedId(it.feedId.toString())
        .setArticleId(it.id.toString())
        .setStreamId(it.streamId.toString())
        .setReleasedAt(it.releasedAt?.time)
        .setStatus(toDto(it.status))
        .setHasFulltext(it.content!!.hasFulltext)
        .setType(toDto(it.type))
        .build() })
      .build()
  }

  private fun toArticleContent(content: ContentEntity): ArticleContentGql? =
    ArticleContentGql.builder()
      .setId(content.id.toString())
      .setTitle(content.title)
      .setImageUrl(content.imageUrl)
      .setUrl(content.url)
      .setDescription(content.description)
      .setContentText(content.contentText)
      .setContentRaw(content.contentRaw)
      .setContentRawMime(content.contentRawMime)
      .setUpdatedAt(content.updatedAt?.time)
      .setCreatedAt(content.createdAt.time)
      .setHasFulltext(content.hasFulltext)
//      .setTags(article.tags)
//      .setContentTitle(article)
      .setPublishedAt(content.publishedAt?.time)
      .setEnclosures(content.attachments?.map { attachment ->
        EnclosureGql.builder().setUrl(attachment.url).setType(attachment.mimeType).build()
      })
      .build()
  private fun toArticle(article: ArticleEntity): ArticleGql? =
    ArticleGql.builder()
      .setId(article.id.toString())
      .setContentId(article.contentId.toString())
      .setStreamId(article.streamId.toString())
      .setFeedId(article.feedId.toString())
      .setContent(toArticleContent(article.content!!))
      .setType(toDto(article.type))
      .setStatus(toDto(article.status))
      .setCreatedAt(article.createdAt.time)
      .build()


  private fun fromDto(status: ReleaseStatusGql): ReleaseStatus {
    return when(status) {
      ReleaseStatusGql.released -> ReleaseStatus.released
      ReleaseStatusGql.needs_approval -> ReleaseStatus.needs_approval
      else -> throw IllegalArgumentException("ReleaseStatus $status not supported")
    }
  }
  private fun toDto(status: ReleaseStatus): ReleaseStatusGql {
    return when(status) {
      ReleaseStatus.released -> ReleaseStatusGql.released
      ReleaseStatus.needs_approval -> ReleaseStatusGql.needs_approval
      else -> throw IllegalArgumentException("ReleaseStatus $status not supported")
    }
  }

  private fun fromDto(type: ArticleTypeGql): ArticleType {
    return when(type) {
      ArticleTypeGql.digest -> ArticleType.digest
      ArticleTypeGql.feed -> ArticleType.feed
      else -> throw IllegalArgumentException("ArticleType $type not supported")
    }
  }

  private fun toDto(type: ArticleType): ArticleTypeGql {
    return when(type) {
      ArticleType.digest -> ArticleTypeGql.digest
      ArticleType.feed -> ArticleTypeGql.feed
      else -> throw IllegalArgumentException("ArticleType $type not supported")
    }
  }

  private fun toGenericFeedGql(it: GenericFeedEntity?): GenericFeedGql? {
    return if (it == null) {
      null
    } else {
      GenericFeedGql.builder()
        .setId(it.id.toString())
        .setNativeFeedId(it.managingFeedId.toString())
        .setFeedRule(JsonUtil.gson.toJson(it.feedRule))
        .setCreatedAt(it.createdAt.time)
        .build()
    }
  }


  private fun toNativeFeedGql(it: NativeFeedEntity): NativeFeedGql {
    return NativeFeedGql.builder()
      .setId(it.id.toString())
      .setTitle(it.title)
      .setDescription(it.description)
      .setWebsiteUrl(it.websiteUrl)
      .setFeedUrl(it.feedUrl)
      .setDomain(it.domain)
      .setGenericFeed(toGenericFeedGql(it.managedBy))
      .setStatus(it.status.toString())
      .setLastUpdatedAt(it.lastUpdatedAt?.time)
      .setCreatedAt(it.createdAt.time)
      .build()
  }

  private fun <T> toPagination(page: Page<T>): PaginationGql? {
    return PaginationGql.builder()
      .setIsEmpty(page.isEmpty)
      .setIsFirst(page.isFirst)
      .setIsLast(page.isLast)
      .setPage(page.number)
      .setTotalElements(page.totalElements)
      .setTotalPages(page.totalPages)
      .build()
  }
}
