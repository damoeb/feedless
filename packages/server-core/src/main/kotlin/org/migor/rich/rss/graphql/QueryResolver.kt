package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.BooleanUtils
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.ArticleDto
import org.migor.rich.rss.generated.ArticlesWhereAndOrderInputDto
import org.migor.rich.rss.generated.BucketDto
import org.migor.rich.rss.generated.BucketsWhereAndOrderInputDto
import org.migor.rich.rss.generated.ContentDto
import org.migor.rich.rss.generated.DiscoverFeedsInputDto
import org.migor.rich.rss.generated.EnclosureDto
import org.migor.rich.rss.generated.FeedDiscoveryResponseDto
import org.migor.rich.rss.generated.GenericFeedDto
import org.migor.rich.rss.generated.ImporterDto
import org.migor.rich.rss.generated.ImporterWhereInputDto
import org.migor.rich.rss.generated.NativeFeedDto
import org.migor.rich.rss.generated.NativeFeedsWhereAndOrderInputDto
import org.migor.rich.rss.generated.PagedArticlesResponseDto
import org.migor.rich.rss.generated.PagedBucketsResponseDto
import org.migor.rich.rss.generated.PagedNativeFeedsResponseDto
import org.migor.rich.rss.generated.RichFeedDto
import org.migor.rich.rss.generated.TransientGenericFeedDto
import org.migor.rich.rss.generated.TransientNativeFeedDto
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.graphql.DtoResolver.toPaginatonDTO
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.GenericFeedService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeType
import java.util.*

@DgsComponent
class QueryResolver {

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var genericFeedService: GenericFeedService

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var feedDiscovery: FeedDiscoveryService

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun bucket(@InputArgument id: String): BucketDto = coroutineScope {
    toDTO(bucketService.findById(UUID.fromString(id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun buckets(@InputArgument data: BucketsWhereAndOrderInputDto): PagedBucketsResponseDto? = coroutineScope {
    val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val page = bucketService.findAllMatching(data.where.query, pageable)

    PagedBucketsResponseDto.builder()
      .setPagination(toPaginatonDTO(page))
      .setBuckets(page.toList().map { toDTO(it) })
      .build()
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun nativeFeeds(@InputArgument data: NativeFeedsWhereAndOrderInputDto): PagedNativeFeedsResponseDto? = coroutineScope {
      val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
      val page = feedService.findAllMatching(data.where.query, pageable)

      PagedNativeFeedsResponseDto.builder()
        .setPagination(toPaginatonDTO(page))
        .setNativeFeeds(page.toList().map { toDTO(it) } )
        .build()
  }

  @DgsQuery
  @Transactional(propagation = Propagation.NEVER)
  suspend fun remoteNativeFeed(@InputArgument nativeFeedUrl: String): RichFeedDto? = coroutineScope {
    val feed = feedService.parseFeedFromUrl(newCorrId(), nativeFeedUrl)
    RichFeedDto.builder()
      .setDescription(feed.description)
      .setTitle(feed.title)
      .setAuthor(feed.author)
      .setFeedUrl(feed.feed_url)
      .setWebsiteUrl(feed.home_page_url)
      .setLanguage(feed.language)
      .setPublishedAt(feed.date_published?.time)
      .setExpired(BooleanUtils.isTrue(feed.expired))
      .setItems(feed.items.map { ContentDto.builder()
        .setTitle(it.title)
        .setDescription(it.contentText)
        .setContentText(it.contentText)
        .setContentRaw(it.contentRaw)
        .setContentRawMime(it.contentRawMime)
        .setPublishedAt(it.publishedAt.time)
        .setUrl(it.url)
        .setImageUrl(it.imageUrl)
        .setEnclosures(it.enclosures?.map { EnclosureDto.builder()
          .setType(it.type)
          .setUrl(it.url)
          .setLength(it.length?.toDouble())
          .build()})
        .build()})
      .build()
  }

  @DgsQuery
  @Transactional(propagation = Propagation.NEVER)
  suspend fun discoverFeeds(@InputArgument data: DiscoverFeedsInputDto): FeedDiscoveryResponseDto = coroutineScope {
    val corrId = handleCorrId(null)
    val discovery = feedDiscovery.discoverFeeds(corrId, data.url, null, BooleanUtils.isTrue(data.prerender), false)
    val response = discovery.results

    FeedDiscoveryResponseDto.builder()
      .setFailed(response.failed)
      .setMimeType(response.mimeType)
      .setHtmlBody(response.mimeType?.let { if (MimeType.valueOf(it).subtype == "html") { response.body } else { null } })
      .setErrorMessage(response.errorMessage)
      .setUrl(data.url)
      .setTitle(response.title)
      .setDescription(response.description)
      .setGenericFeeds(response.genericFeedRules.map { TransientGenericFeedDto.builder()
        .setFeedUrl(it.feedUrl)
        .setCount(it.count)
        .setContextXPath(it.contextXPath)
        .setDateXPath(it.dateXPath)
        .setExtendContext(it.extendContext)
        .setLinkXPath(it.linkXPath)
        .setScore(it.score)
        .build()
      })
      .setNativeFeeds(response.nativeFeeds.map { TransientNativeFeedDto.builder()
        .setUrl(it.url)
        .setTitle(it.title)
        .setType(it.type!!.name)
        .setDescription(it.description)
        .build()
      })
      .build()
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun article(@InputArgument id: String): ArticleDto? = coroutineScope {
    toDTO(articleService.findById(UUID.fromString(id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun content(@InputArgument id: String): ContentDto? = coroutineScope {
    toDTO(articleService.findContentById(UUID.fromString(id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun nativeFeed(@InputArgument id: String): NativeFeedDto? = coroutineScope {
    toDTO(feedService.findNativeById(UUID.fromString(id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun genericFeed(@InputArgument id: String): GenericFeedDto? = coroutineScope {
    toDTO(genericFeedService.findById(UUID.fromString(id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun importer(@InputArgument data: ImporterWhereInputDto): ImporterDto? = coroutineScope {
    if (data.importerId != null) {
      importerService.findById(UUID.fromString(data.importerId)).map { toDTO(it) }.orElseThrow()
    } else {
      val nativeFeedId = UUID.fromString(data.bucketAndFeed.nativeFeedId)
      val bucketId = UUID.fromString(data.bucketAndFeed.bucketId)
      importerService.findByBucketAndFeed(bucketId, nativeFeedId).map { toDTO(it) }.orElseThrow()
    }
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun articles(@InputArgument data: ArticlesWhereAndOrderInputDto): PagedArticlesResponseDto = coroutineScope {
    toDTO(articleService.findAllFiltered(data))
  }
}
