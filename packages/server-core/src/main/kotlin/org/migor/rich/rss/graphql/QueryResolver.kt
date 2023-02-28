package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.ApiUrls
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.types.*
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.graphql.DtoResolver.toPaginatonDTO
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.ContentService
import org.migor.rich.rss.service.FeatureToggleService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.GenericFeedService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.migor.rich.rss.util.GenericFeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeType
import java.util.*
import org.migor.rich.rss.generated.types.ApiUrls as ApiUrlsDto

@DgsComponent
class QueryResolver {

  private val log = LoggerFactory.getLogger(QueryResolver::class.simpleName)

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var genericFeedService: GenericFeedService

  @Autowired
  lateinit var propertyService: PropertyService

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

  @Autowired
  lateinit var featureToggleService: FeatureToggleService

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun bucket(@InputArgument data: BucketWhereInput): Bucket = coroutineScope {
    toDTO(bucketService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun buckets(@InputArgument data: BucketsPagedInput): PagedBucketsResponse? = coroutineScope {
    val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val buckets = bucketService.findAllMatching(data.where.query, pageable)

    PagedBucketsResponse.newBuilder()
      .pagination(toPaginatonDTO(pageable, buckets))
      .buckets(buckets.toList().map { toDTO(it) })
      .build()
  }

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun nativeFeeds(@InputArgument data: NativeFeedsInput): PagedNativeFeedsResponse? = coroutineScope {
    val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val feeds = if (StringUtils.isBlank(data.where.feedUrl)) {
      feedService.findAllByFilter(data.where, pageable)
    } else {
      feedService.findAllByFeedUrl(data.where.feedUrl!!, pageable)
    }
    PagedNativeFeedsResponse.newBuilder()
      .pagination(toPaginatonDTO(pageable, feeds))
      .nativeFeeds(feeds.toList().map { toDTO(it) })
      .build()
  }

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun genericFeeds(@InputArgument data: GenericFeedsInput): PagedGenericFeedsResponse? = coroutineScope {
    val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val feeds = genericFeedService.findAllByFilter(data.where, pageable)
    PagedGenericFeedsResponse.newBuilder()
      .pagination(toPaginatonDTO(pageable, feeds))
      .genericFeeds(feeds.toList().map { toDTO(it) })
      .build()
  }

  @DgsQuery
  suspend fun serverSettings(): ServerSettings = coroutineScope {
    ServerSettings.newBuilder()
      .apiUrls(
        ApiUrlsDto.newBuilder()
          .webToFeed("${propertyService.publicUrl}${ApiUrls.webToFeed}")
          .build()
      )
      .featureToggles(mapOf(
        FeatureName.authentication to featureToggleService.withAuthentication(),
        FeatureName.database to featureToggleService.withDatabase(),
        FeatureName.puppeteer to featureToggleService.withPuppeteer(),
        FeatureName.elasticsearch to featureToggleService.withElasticSearch(),
      ).map {
        FeatureToggle.newBuilder()
          .name(it.key)
          .enabled(it.value)
          .build()
      }
      ).build()
  }


  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.NEVER)
  suspend fun remoteNativeFeed(@InputArgument nativeFeedUrl: String): RemoteNativeFeed? = coroutineScope {
    val feed = feedService.parseFeedFromUrl(newCorrId(), nativeFeedUrl)
    RemoteNativeFeed.newBuilder()
      .description(feed.description)
      .title(feed.title)
//      Author=feed.author,
      .feedUrl(feed.feedUrl)
      .websiteUrl(feed.websiteUrl)
      .language(feed.language)
      .publishedAt(feed.publishedAt.time)
      .expired(BooleanUtils.isTrue(feed.expired))
      .items(feed.items.map {
        Content.newBuilder()
          .title(it.title)
          .description(it.contentText)
          .contentText(it.contentText)
          .contentRaw(it.contentRaw)
          .contentRawMime(it.contentRawMime)
          .publishedAt(it.publishedAt.time)
          .startingAt(it.startingAt?.time)
          .url(it.url)
          .imageUrl(it.imageUrl)
          .enclosures(it.attachments.map {
            Enclosure.newBuilder()
              .type(it.type)
              .url(it.url)
              .length(it.length?.toDouble())
              .build()
          })
          .build()
      })
      .build()
  }

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.NEVER)
  suspend fun discoverFeeds(@InputArgument data: DiscoverFeedsInput): FeedDiscoveryResponse = coroutineScope {
    val corrId = handleCorrId(null)
    val fetchOptions = GenericFeedUtil.fromDto(data.fetchOptions)
    val discovery = feedDiscovery.discoverFeeds(corrId, fetchOptions)
    val response = discovery.results

    FeedDiscoveryResponse.newBuilder()
      .failed(response.failed)
      .errorMessage(response.errorMessage)
      .document(Optional.ofNullable(response.document).map { document ->
        FeedDiscoveryDocument.newBuilder()
          .mimeType(document.mimeType)
          .htmlBody(document.mimeType?.let {
            if (MimeType.valueOf(it).subtype == "html") {
              document.body
            } else {
              null
            }
          }
          )
          .title(document.title)
          .language(document.language)
          .description(document.description)
          .imageUrl(document.imageUrl)
          .build()
      }.orElse(null))
      .websiteUrl(discovery.options.harvestUrl)
      .nativeFeeds(response.nativeFeeds.map {
        TransientNativeFeed.newBuilder()
          .url(it.url)
          .title(it.title)
          .type(it.type.name)
          .description(it.description)
          .build()
      })
      .genericFeeds(
        GenericFeeds.newBuilder()
          .parserOptions(GenericFeedUtil.toDto(data.parserOptions))
          .fetchOptions(GenericFeedUtil.toDto(data.fetchOptions))
//        .feeds = ,
          .feeds(response.genericFeedRules.map {
            val selectors = Selectors.newBuilder()
              .contextXPath(it.contextXPath)
              .dateXPath(StringUtils.trimToEmpty(it.dateXPath))
              .extendContext(GenericFeedUtil.toDto(it.extendContext))
              .linkXPath(it.linkXPath)
              .paginationXPath(StringUtils.trimToEmpty(it.paginationXPath))
              .dateIsStartOfEvent(it.dateIsStartOfEvent)
              .build()
            TransientGenericFeed.newBuilder()
              .feedUrl(it.feedUrl)
              .count(it.count)
              .hash(feedService.toHash(selectors))
              .selectors(selectors)
              .score(it.score)
              .samples(it.samples.map {
                Content.newBuilder()
                  .id(UUID.randomUUID().toString())
                  .url(it.url)
                  .title(it.title)
                  .contentText(it.contentText)
                  .description(it.contentText)
                  .contentRaw(it.contentRaw)
                  .contentRawMime(it.contentRawMime)
                  .publishedAt(it.publishedAt.time)
                  .updatedAt(it.publishedAt.time)
                  .createdAt(Date().time)
                  .build()
              }
              ).build()
          })
          .build()
      )
      .build()
  }

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun article(@InputArgument data: ArticleWhereInput): Article = coroutineScope {
    toDTO(articleService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun content(@InputArgument data: ContentWhereInput): Content = coroutineScope {
    toDTO(contentService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun nativeFeed(@InputArgument data: NativeFeedWhereInput): NativeFeed = coroutineScope {
    toDTO(feedService.findNativeById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun genericFeed(@InputArgument data: GenericFeedWhereInput): GenericFeed? = coroutineScope {
    toDTO(genericFeedService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun importer(@InputArgument data: ImporterWhereInput): Importer? = coroutineScope {
    if (data.importer != null) {
      importerService.findById(UUID.fromString(data.importer.id)).map { toDTO(it) }.orElseThrow()
    } else {
      val nativeFeedId = UUID.fromString(data.bucketAndFeed!!.nativeFeed.id)
      val bucketId = UUID.fromString(data.bucketAndFeed.bucket.id)
      importerService.findByBucketAndFeed(bucketId, nativeFeedId).map { toDTO(it) }.orElseThrow()
    }
  }

  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun articles(@InputArgument data: ArticlesPagedInput): PagedArticlesResponse = coroutineScope {
    val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val items = articleService.findAllFiltered(data)
    PagedArticlesResponse.newBuilder()
      .pagination(toPaginatonDTO(pageable, items))
      .articles(items.toList().map { toDTO(it) })
      .build()
  }
}
