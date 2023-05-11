package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.Throttled
import org.migor.feedless.api.auth.CookieProvider
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.api.graphql.DtoResolver.fromDTO
import org.migor.feedless.api.graphql.DtoResolver.toDTO
import org.migor.feedless.api.graphql.DtoResolver.toPaginatonDTO
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.models.BucketEntity
import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.migor.feedless.feed.discovery.FeedDiscoveryService
import org.migor.feedless.generated.types.*
import org.migor.feedless.service.ArticleService
import org.migor.feedless.service.BucketOrNativeFeedService
import org.migor.feedless.service.BucketService
import org.migor.feedless.service.FeatureToggleService
import org.migor.feedless.service.FeedParserService
import org.migor.feedless.service.FeedService
import org.migor.feedless.service.FilterService
import org.migor.feedless.service.GenericFeedService
import org.migor.feedless.service.ImporterService
import org.migor.feedless.service.PlanService
import org.migor.feedless.service.PluginsService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.WebDocumentService
import org.migor.feedless.util.GenericFeedUtil
import org.migor.feedless.util.GenericFeedUtil.toDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.context.request.ServletWebRequest
import java.util.*
import org.migor.feedless.generated.types.ApiUrls as ApiUrlsDto

@DgsComponent
@org.springframework.context.annotation.Profile(AppProfiles.database)
class QueryResolver {

  private val log = LoggerFactory.getLogger(QueryResolver::class.simpleName)
  private val pageSize = 20

  @Autowired
  lateinit var currentUser: CurrentUser

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var cookieProvider: CookieProvider

  @Autowired
  lateinit var genericFeedService: GenericFeedService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var bucketOrNativeFeedService: BucketOrNativeFeedService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var webDocumentService: WebDocumentService

  @Autowired
  lateinit var featureToggleService: FeatureToggleService

  @Autowired
  lateinit var planService: PlanService

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun bucket(
    @InputArgument data: BucketWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String
  ): Bucket = coroutineScope {
    log.info("[$corrId] bucket $data")
    toDTO(bucketService.findById(UUID.fromString(data.where.id))
      .orElseThrow { IllegalArgumentException("bucket not found") } )
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun buckets(
    @InputArgument data: BucketsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): BucketsResponse? = coroutineScope {
    log.info("[$corrId] buckets $data")
    val pageable = PageRequest.of(handlePageNumber(data.cursor.page), pageSize, fromDTO(data.orderBy))
    val buckets = bucketService.findAllMatching(data.where, pageable)

    BucketsResponse.newBuilder()
      .pagination(toPaginatonDTO(pageable, buckets))
      .buckets(buckets.toList().map { toDTO(it) })
      .build()
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun bucketsOrNativeFeeds(
    @InputArgument data: BucketsOrNativeFeedsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<BucketOrNativeFeed> = coroutineScope {
    log.info("[$corrId] buckets $data")
    val pageNumber = handlePageNumber(data.cursor.page)
    val pageSize = handlePageSize(data.cursor.pageSize)
    val offset = pageNumber * pageSize
    bucketOrNativeFeedService.findAll(offset, pageSize)
      .map {
        BucketOrNativeFeed.newBuilder()
          .bucket(if (it is BucketEntity) toDTO(it) else null)
          .feed(if (it is NativeFeedEntity) toDTO(it) else null)
          .build()
      }
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun nativeFeeds(
    @InputArgument data: NativeFeedsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): NativeFeedsResponse? = coroutineScope {
    log.info("[$corrId] nativeFeeds $data")
    val pageable = PageRequest.of(handlePageNumber(data.cursor.page), pageSize, fromDTO(data.orderBy))
    val feeds = if (StringUtils.isBlank(data.where.feedUrl)) {
      feedService.findAllByFilter(data.where, pageable)
    } else {
      feedService.findAllByFeedUrl(data.where.feedUrl!!, pageable)
    }
    NativeFeedsResponse.newBuilder()
      .pagination(toPaginatonDTO(pageable, feeds))
      .nativeFeeds(feeds.toList().map { toDTO(it) })
      .build()
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun genericFeeds(
    @InputArgument data: GenericFeedsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): GenericFeedsResponse? = coroutineScope {
    log.info("[$corrId] genericFeeds $data")
    val pageable = PageRequest.of(handlePageNumber(data.cursor.page), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
    val feeds = genericFeedService.findAllByFilter(data.where, pageable)
    GenericFeedsResponse.newBuilder()
      .pagination(toPaginatonDTO(pageable, feeds))
      .genericFeeds(feeds.toList().map { toDTO(it) })
      .build()
  }

  @DgsQuery
  @Cacheable(value = [CacheNames.GRAPHQL_RESPONSE], key = "'serverSettings'")
  suspend fun serverSettings(): ServerSettings = coroutineScope {
    val db = featureToggleService.withDatabase()
    val es = featureToggleService.withElasticSearch()
    val authMail = environment.acceptsProfiles(Profiles.of(AppProfiles.authMail))
    val authSSSO = environment.acceptsProfiles(Profiles.of(AppProfiles.authSSO))
    ServerSettings.newBuilder()
      .apiUrls(
        ApiUrlsDto.newBuilder()
          .webToFeed("${propertyService.apiGatewayUrl}${ApiUrls.webToFeedFromRule}")
          .build()
      )
      .features(mapOf(
        FeatureName.database to stable(db),
        FeatureName.puppeteer to stable(featureToggleService.withPuppeteer()),
        FeatureName.elasticsearch to experimental(es),
        FeatureName.genFeedFromFeed to stable(),
        FeatureName.genFeedFromPageChange to FeatureState.off,
        FeatureName.genFeedFromWebsite to stable(),
        FeatureName.authMail to stable(authMail),
        FeatureName.authSSO to stable(authSSSO),
        FeatureName.authAllowRoot to stable(!authMail, !authSSSO),
      ).map {
        feature(it.key, it.value)
      }
      ).build()
  }

  private fun feature(name: FeatureName, state: FeatureState): Feature = Feature.newBuilder()
    .name(name)
    .state(state)
    .build()


  private fun stable(vararg requirements: Boolean): FeatureState {
    return if (requirements.isNotEmpty() && requirements.all { it }) {
      FeatureState.stable
    } else {
      FeatureState.off
    }
  }

  private fun experimental(vararg requirements: Boolean): FeatureState {
    return if (requirements.isNotEmpty() && requirements.all { it }) {
      FeatureState.experimental
    } else {
      FeatureState.off
    }
  }

  @DgsQuery
//  @Cacheable(value = [CacheNames.GRAPHQL_RESPONSE], key = "'profile'", unless = "#result.isLoggedIn==true")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun profile(dfe: DataFetchingEnvironment): Profile = coroutineScope {
    unsetSessionCookie(dfe)
    val defaultProfile = Profile.newBuilder()
      .preferReader(true)
      .preferFulltext(true)
      .isLoggedIn(false)
      .dateFormat(propertyService.dateFormat)
      .timeFormat(propertyService.timeFormat)
      .minimalFeatureState(FeatureState.experimental)
      .build()

    if (currentUser.isUser()) {
      runCatching {
        val user = currentUser.user()
        Profile.newBuilder()
          .preferReader(true)
          .preferFulltext(true)
          .dateFormat(propertyService.dateFormat)
          .timeFormat(propertyService.timeFormat)
          .isLoggedIn(true)
          .userId(user.id.toString())
          .minimalFeatureState(FeatureState.experimental)
          .build()
      }.getOrDefault(defaultProfile)
    } else {
      defaultProfile

    }
  }

  private fun unsetSessionCookie(dfe: DataFetchingEnvironment) {
    val cookie = cookieProvider.createExpiredSessionCookie("JSESSION")
    ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!.addCookie(cookie)
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun article(
    @InputArgument data: ArticleWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Article = coroutineScope {
    log.info("[$corrId] article $data")
    toDTO(articleService.findById(UUID.fromString(data.where.id))
      .orElseThrow { IllegalArgumentException("article not found") })
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun webDocument(
    @InputArgument data: WebDocumentWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): WebDocument = coroutineScope {
    log.info("[$corrId] content $data")
    toDTO(webDocumentService.findById(UUID.fromString(data.where.id))
      .orElseThrow { IllegalArgumentException("webDocument not found")} )
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun nativeFeed(
    @InputArgument data: NativeFeedWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): NativeFeed = coroutineScope {
    log.info("[$corrId] nativeFeed $data")
    toDTO(feedService.findNativeById(UUID.fromString(data.where.id))
      .orElseThrow { IllegalArgumentException("nativeFeed not found") } )
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun genericFeed(
    @InputArgument data: GenericFeedWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): GenericFeed? = coroutineScope {
    log.info("[$corrId] genericFeed $data")
    toDTO(genericFeedService.findById(UUID.fromString(data.where.id))
      .orElseThrow { IllegalArgumentException("genericFeed not found") })
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun importers(
    @InputArgument data: ImportersInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): ImportersResponse = coroutineScope {
    log.info("[$corrId] importers $data")
    val pageable = PageRequest.of(handlePageNumber(data.cursor.page), pageSize, fromDTO(data.orderBy))
    val items = importerService.findAllByFilter(data.where, pageable).map { toDTO(it) }
    ImportersResponse.newBuilder()
      .pagination(toPaginatonDTO(pageable, items))
      .importers(items)
      .build()
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun articles(
    @InputArgument data: ArticlesInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): ArticlesResponse = coroutineScope {
    log.info("[$corrId] articles ${data} -> ${fromDTO(data.orderBy)}")
    val pageable = PageRequest.of(handlePageNumber(data.cursor.page), handlePageSize(data.cursor.pageSize), fromDTO(data.orderBy))
    val items = articleService.findAllByFilter(data.where, pageable)
    ArticlesResponse.newBuilder()
      .pagination(toPaginatonDTO(pageable, items))
      .articles(items.map { toDTO(it) })
      .build()
  }

  private fun handlePageNumber(page: Int?): Int =
    page ?: 0

  private fun handlePageSize(pageSize: Int?): Int =
    (pageSize ?: this.pageSize).coerceAtLeast(1).coerceAtMost(this.pageSize)

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun plans(@RequestHeader(ApiParams.corrId) corrId: String,): List<Plan> = coroutineScope {
    log.info("[$corrId] plans")
    planService.findAll().map { Plan.newBuilder()
      .id(it.id.toString())
      .name(toDTO(it.name))
      .costs(it.costs)
      .availability(toDTO(it.availability))
      .isPrimary(it.primary)
      .build()
    }
  }
}
