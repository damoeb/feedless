package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.ApiUrls
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.*
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.graphql.DtoResolver.toPaginatonDTO
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.ContentService
import org.migor.rich.rss.service.FeatureToggleService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.GenericFeedService
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.PuppeteerService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.migor.rich.rss.util.GenericFeedUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeType
import java.util.*

@DgsComponent
class QueryResolver {

  @Autowired(required = false)
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var genericFeedService: GenericFeedService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired(required = false)
  lateinit var importerService: ImporterService

  @Autowired(required = false)
  lateinit var bucketService: BucketService

  @Autowired(required = false)
  lateinit var feedService: FeedService

  @Autowired(required = false)
  lateinit var contentService: ContentService

  @Autowired
  lateinit var feedDiscovery: FeedDiscoveryService

  @Autowired
  lateinit var featureToggleService: FeatureToggleService

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
  suspend fun nativeFeeds(@InputArgument data: NativeFeedsInputDto): PagedNativeFeedsResponseDto? = coroutineScope {
    val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val page = if (StringUtils.isBlank(data.where.feedUrl)) {
      feedService.findAllByFilter(data.where, pageable)
    } else {
      feedService.findAllByFeedUrl(data.where.feedUrl, pageable)
    }
    PagedNativeFeedsResponseDto.builder()
      .setPagination(toPaginatonDTO(page))
      .setNativeFeeds(page.toList().map { toDTO(it) } )
      .build()

  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun genericFeeds(@InputArgument data: GenericFeedsInputDto): PagedGenericFeedsResponseDto? = coroutineScope {
    val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val page = genericFeedService.findAllByFilter(data.where, pageable)
    PagedGenericFeedsResponseDto.builder()
      .setPagination(toPaginatonDTO(page))
      .setGenericFeeds(page.toList().map { toDTO(it) } )
      .build()

  }

  @DgsQuery
  suspend fun serverSettings(): ServerSettingsDto = coroutineScope {
    ServerSettingsDto.builder()
      .setApiUrls(ApiUrlsDto.builder()
        .setWebToFeed("${propertyService.publicUrl}${ApiUrls.webToFeed}")
        .build())
      .setFeatureToggles(mapOf(
        FeatureNameDto.authentication to featureToggleService.withAuthentication(),
        FeatureNameDto.database to featureToggleService.withDatabase(),
        FeatureNameDto.puppeteer to featureToggleService.withPuppeteer(),
        FeatureNameDto.elasticsearch to featureToggleService.withElasticSearch(),
      ).map { FeatureToggleDto.builder()
        .setName(it.key)
        .setEnabled(it.value)
        .build() }
      )
      .build()
  }


  @DgsQuery
  @Transactional(propagation = Propagation.NEVER)
  suspend fun remoteNativeFeed(@InputArgument nativeFeedUrl: String): RemoteNativeFeedDto? = coroutineScope {
    val feed = feedService.parseFeedFromUrl(newCorrId(), nativeFeedUrl)
    RemoteNativeFeedDto.builder()
      .setDescription(feed.description)
      .setTitle(feed.title)
//      .setAuthor(feed.author)
      .setFeedUrl(feed.feedUrl)
      .setWebsiteUrl(feed.websiteUrl)
      .setLanguage(feed.language)
      .setPublishedAt(feed.publishedAt?.time)
      .setExpired(BooleanUtils.isTrue(feed.expired))
      .setItems(feed.items.map { ContentDto.builder()
        .setTitle(it.title)
        .setDescription(it.contentText)
        .setContentText(it.contentText)
        .setContentRaw(it.contentRaw)
        .setContentRawMime(it.contentRawMime)
        .setPublishedAt(it.publishedAt.time)
        .setStartingAt(it.startingAt?.time)
        .setUrl(it.url)
        .setImageUrl(it.imageUrl)
        .setEnclosures(it.attachments.map { EnclosureDto.builder()
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
    val fetchOptions = GenericFeedUtil.fromDto(data.fetchOptions)
    val discovery = feedDiscovery.discoverFeeds(corrId, fetchOptions)
    val response = discovery.results

    FeedDiscoveryResponseDto.builder()
      .setFailed(response.failed)
      .setErrorMessage(response.errorMessage)
      .setDocument(Optional.ofNullable(response.document).map { document ->
        FeedDiscoveryDocumentDto.builder()
          .setMimeType(document.mimeType)
          .setHtmlBody(document.mimeType?.let {
            if (MimeType.valueOf(it).subtype == "html") {
              document.body
            } else {
              null
            }
          })
          .setTitle(document.title)
          .setLanguage(document.language)
          .setDescription(document.description)
          .setImageUrl(document.imageUrl)
          .build()
      }.orElse(null))
      .setWebsiteUrl(discovery.options.harvestUrl)
      .setGenericFeeds(GenericFeedsDto.builder()
        .setParserOptions(GenericFeedUtil.toDto(data.parserOptions))
        .setFetchOptions(GenericFeedUtil.toDto(data.fetchOptions))
        .setFeeds(response.genericFeedRules.map {
          val selectors = SelectorsDto.builder()
            .setContextXPath(it.contextXPath)
            .setDateXPath(StringUtils.trimToEmpty(it.dateXPath))
            .setExtendContext(GenericFeedUtil.toDto(it.extendContext))
            .setLinkXPath(it.linkXPath)
            .build()
          TransientGenericFeedDto.builder()
            .setFeedUrl(it.feedUrl)
            .setCount(it.count)
            .setHash(feedService.toHash(selectors))
            .setSelectors(selectors)
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
        }
        ).build())
      .setNativeFeeds(response.nativeFeeds.map {
        TransientNativeFeedDto.builder()
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
