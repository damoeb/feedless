package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.BooleanUtils
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.ArticleDto
import org.migor.rich.rss.generated.ArticleWhereInputDto
import org.migor.rich.rss.generated.ArticlesPagedInputDto
import org.migor.rich.rss.generated.BucketDto
import org.migor.rich.rss.generated.BucketWhereInputDto
import org.migor.rich.rss.generated.BucketsPagedInputDto
import org.migor.rich.rss.generated.ContentDto
import org.migor.rich.rss.generated.ContentWhereInputDto
import org.migor.rich.rss.generated.DiscoverFeedsInputDto
import org.migor.rich.rss.generated.EnclosureDto
import org.migor.rich.rss.generated.FeedDiscoveryResponseDto
import org.migor.rich.rss.generated.GenericFeedDto
import org.migor.rich.rss.generated.GenericFeedWhereInputDto
import org.migor.rich.rss.generated.ImporterDto
import org.migor.rich.rss.generated.ImporterWhereInputDto
import org.migor.rich.rss.generated.NativeFeedDto
import org.migor.rich.rss.generated.NativeFeedWhereInputDto
import org.migor.rich.rss.generated.NativeFeedsPagedInputDto
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
import org.migor.rich.rss.service.ContentService
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
  lateinit var contentService: ContentService

  @Autowired
  lateinit var feedDiscovery: FeedDiscoveryService

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun bucket(@InputArgument data: BucketWhereInputDto): BucketDto = coroutineScope {
    toDTO(bucketService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun buckets(@InputArgument data: BucketsPagedInputDto): PagedBucketsResponseDto? = coroutineScope {
    val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val page = bucketService.findAllMatching(data.where.query, pageable)

    PagedBucketsResponseDto.builder()
      .setPagination(toPaginatonDTO(page))
      .setBuckets(page.toList().map { toDTO(it) })
      .build()
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun nativeFeeds(@InputArgument data: NativeFeedsPagedInputDto): PagedNativeFeedsResponseDto? = coroutineScope {
      val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
      val page = feedService.findAllByFilter(data.where, pageable)

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
        .setSamples(it.samples.map {
          ContentDto.builder()
            .setId(UUID.randomUUID().toString())
            .setUrl(it.url)
            .setTitle(it.title)
            .setContentText(it.contentText)
            .setDescription(it.contentText)
            .setContentRaw(it.contentRaw)
            .setContentRawMime(it.contentRawMime)
            .setPublishedAt(it.publishedAt.time)
            .setUpdatedAt(it.publishedAt.time)
            .setCreatedAt(Date().time)
          .build()
        })
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
  suspend fun article(@InputArgument data: ArticleWhereInputDto): ArticleDto? = coroutineScope {
    toDTO(articleService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun content(@InputArgument data: ContentWhereInputDto): ContentDto? = coroutineScope {
    toDTO(contentService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun nativeFeed(@InputArgument data: NativeFeedWhereInputDto): NativeFeedDto? = coroutineScope {
    toDTO(feedService.findNativeById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun genericFeed(@InputArgument data: GenericFeedWhereInputDto): GenericFeedDto? = coroutineScope {
    toDTO(genericFeedService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun importer(@InputArgument data: ImporterWhereInputDto): ImporterDto? = coroutineScope {
    if (data.importer != null) {
      importerService.findById(UUID.fromString(data.importer.id)).map { toDTO(it) }.orElseThrow()
    } else {
      val nativeFeedId = UUID.fromString(data.bucketAndFeed.nativeFeed.id)
      val bucketId = UUID.fromString(data.bucketAndFeed.bucket.id)
      importerService.findByBucketAndFeed(bucketId, nativeFeedId).map { toDTO(it) }.orElseThrow()
    }
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun articles(@InputArgument data: ArticlesPagedInputDto): PagedArticlesResponseDto = coroutineScope {
    toDTO(articleService.findAllFiltered(data))
  }
}
