package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import jakarta.servlet.http.Cookie
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.ApiUrls
import org.migor.rich.rss.auth.CurrentUser
import org.migor.rich.rss.config.CacheNames
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.types.*
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.graphql.DtoResolver.toPaginatonDTO
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.ContentService
import org.migor.rich.rss.service.FeatureService
import org.migor.rich.rss.service.FeatureToggleService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.GenericFeedService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.service.PlanService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.UserService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.migor.rich.rss.util.GenericFeedUtil
import org.migor.rich.rss.util.GenericFeedUtil.toDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeType
import org.springframework.web.context.request.ServletWebRequest
import java.util.*
import org.migor.rich.rss.generated.types.ApiUrls as ApiUrlsDto

@DgsComponent
class QueryResolver {

  private val log = LoggerFactory.getLogger(QueryResolver::class.simpleName)

  @Autowired
  lateinit var currentUser: CurrentUser

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var genericFeedService: GenericFeedService

  @Autowired
  lateinit var userService: UserService

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

  @Autowired
  lateinit var planService: PlanService

  @Autowired
  lateinit var featureService: FeatureService

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun bucket(@InputArgument data: BucketWhereInput): Bucket = coroutineScope {
    toDTO(bucketService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
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
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun nativeFeeds(@InputArgument data: NativeFeedsPagedInput): PagedNativeFeedsResponse? = coroutineScope {
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
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun genericFeeds(@InputArgument data: GenericFeedsPagedInput): PagedGenericFeedsResponse? = coroutineScope {
    val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val feeds = genericFeedService.findAllByFilter(data.where, pageable)
    PagedGenericFeedsResponse.newBuilder()
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
          .webToFeed("${propertyService.publicUrl}${ApiUrls.webToFeed}")
          .build()
      )
      .features(mapOf(
        FeatureName.database to stable(db),
        FeatureName.puppeteer to stable(featureToggleService.withPuppeteer()),
        FeatureName.elasticsearch to experimental(es),
        FeatureName.genFeedFromFeed to stable(),
        FeatureName.genFeedFromPageChange to experimental(es, db),
        FeatureName.genFeedFromWebsite to stable(),
        FeatureName.authMail to stable(authMail),
        FeatureName.authSSO to stable(authSSSO),
        FeatureName.authenticated to FeatureState.off,
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
  @Transactional(propagation = Propagation.NEVER)
  suspend fun profile(dfe: DataFetchingEnvironment): Profile = coroutineScope {
    unsetSessionCookie(dfe)
    log.info(SecurityContextHolder.getContext().authentication.toString())
    if (currentUser.isUser()) {
      val user = currentUser.user()
      Profile.newBuilder()
        .preferReader(true)
        .preferFulltext(true)
        .dateFormat(propertyService.dateFormat)
        .timeFormat(propertyService.timeFormat)
        .isLoggedIn(true)
        .user(User.newBuilder()
          .id(user.id.toString())
          .createdAt(user.createdAt.time)
          .name(user.name)
          .acceptedTermsAndServices(user.hasApprovedTerms)
          .notificationsStreamId(user.notificationsStreamId!!.toString())
          .build())
        .minimalFeatureState(FeatureState.experimental)
        .featuresOverwrites(
          listOf(
            feature(FeatureName.authenticated, FeatureState.stable)
          )
        )
        .build()
    } else {
      Profile.newBuilder()
        .preferReader(true)
        .preferFulltext(true)
        .isLoggedIn(false)
        .dateFormat(propertyService.dateFormat)
        .timeFormat(propertyService.timeFormat)
        .minimalFeatureState(FeatureState.experimental)
        .featuresOverwrites(
          emptyList()
        )
        .build()

    }
  }

  private fun unsetSessionCookie(dfe: DataFetchingEnvironment) {
    val cookie = Cookie("JSESSIONID", "")
    cookie.isHttpOnly = true
//    cookie.domain = propertyService.domain
    cookie.maxAge = 0
    ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!.addCookie(cookie)
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

    val document = response.document
    FeedDiscoveryResponse.newBuilder()
      .failed(response.failed)
      .errorMessage(response.errorMessage)
      .document(FeedDiscoveryDocument.newBuilder()
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
          .build())
      .websiteUrl(discovery.options.harvestUrl)
      .nativeFeeds(response.nativeFeeds.map {toDto(it)
      })
      .genericFeeds(
        GenericFeeds.newBuilder()
          .parserOptions(toDto(data.parserOptions))
          .fetchOptions(toDto(data.fetchOptions))
          .feeds(response.genericFeedRules.map {toDto(it) })
          .build()
      )
      .build()
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun article(@InputArgument data: ArticleWhereInput): Article = coroutineScope {
    toDTO(articleService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun content(@InputArgument data: ContentWhereInput): Content = coroutineScope {
    toDTO(contentService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun nativeFeed(@InputArgument data: NativeFeedWhereInput): NativeFeed = coroutineScope {
    toDTO(feedService.findNativeById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun genericFeed(@InputArgument data: GenericFeedWhereInput): GenericFeed? = coroutineScope {
    toDTO(genericFeedService.findById(UUID.fromString(data.where.id)).orElseThrow())
  }

  @DgsQuery
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
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun articles(@InputArgument data: ArticlesPagedInput): PagedArticlesResponse = coroutineScope {
    val pageable = PageRequest.of(data.page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val items = articleService.findAllFiltered(data)
    PagedArticlesResponse.newBuilder()
      .pagination(toPaginatonDTO(pageable, items))
      .articles(items.toList().map { toDTO(it) })
      .build()
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun plans(): List<Plan> = coroutineScope {
    planService.findAll().map { Plan.newBuilder()
      .planName(toDTO(it.name))
      .costs(it.costs)
      .availability(toDTO(it.availability))
      .isPrimary(it.primary)
      .features(featureService.resolveByPlanName(it.name).map { toDTO(it) })
      .build()
    }
  }
}
