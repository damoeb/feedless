package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import jakarta.servlet.http.Cookie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.migor.rich.rss.api.ApiParams
import org.migor.rich.rss.auth.CurrentUser
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.BucketEntity
import org.migor.rich.rss.data.jpa.models.GenericFeedEntity
import org.migor.rich.rss.data.jpa.models.ImporterEntity
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.repositories.GenericFeedDAO
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.types.ArticleCreateInput
import org.migor.rich.rss.generated.types.ArticlesDeleteWhereInput
import org.migor.rich.rss.generated.types.ArticlesUpdateWhereInput
import org.migor.rich.rss.generated.types.Bucket
import org.migor.rich.rss.generated.types.BucketCreateInput
import org.migor.rich.rss.generated.types.BucketCreateOrConnectInput
import org.migor.rich.rss.generated.types.BucketDeleteInput
import org.migor.rich.rss.generated.types.BucketUpdateInput
import org.migor.rich.rss.generated.types.ConfirmAuthCodeInput
import org.migor.rich.rss.generated.types.GenericFeed
import org.migor.rich.rss.generated.types.GenericFeedCreateInput
import org.migor.rich.rss.generated.types.GenericFeedDeleteInput
import org.migor.rich.rss.generated.types.Importer
import org.migor.rich.rss.generated.types.ImporterDeleteInput
import org.migor.rich.rss.generated.types.ImportersCreateInput
import org.migor.rich.rss.generated.types.NativeFeed
import org.migor.rich.rss.generated.types.NativeFeedCreateInput
import org.migor.rich.rss.generated.types.NativeFeedCreateOrConnectInput
import org.migor.rich.rss.generated.types.NativeFeedDeleteInput
import org.migor.rich.rss.graphql.DtoResolver.fromDTO
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.http.Throttled
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.DefaultsService
import org.migor.rich.rss.service.FilterService
import org.migor.rich.rss.service.GenericFeedService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.service.MailAuthenticationService
import org.migor.rich.rss.service.NativeFeedService
import org.migor.rich.rss.service.OpmlService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.auth.TokenProvider
import org.migor.rich.rss.generated.types.NativeFeedUpdateInput
import org.migor.rich.rss.service.UserService
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import org.migor.rich.rss.transform.WebToFeedTransformer
import org.migor.rich.rss.util.CryptUtil
import org.migor.rich.rss.util.GenericFeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.context.request.ServletWebRequest
import java.net.URL
import java.util.*
import org.migor.rich.rss.generated.types.Authentication as AuthenticationDto

@DgsComponent
class MutationResolver {

  private val log = LoggerFactory.getLogger(MutationResolver::class.simpleName)

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var mailAuthenticationService: MailAuthenticationService

  @Autowired
  lateinit var feedDiscoveryService: FeedDiscoveryService

  @Autowired
  lateinit var nativeFeedService: NativeFeedService

  @Autowired
  lateinit var opmlService: OpmlService

  @Autowired
  lateinit var defaultsService: DefaultsService

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var genericFeedService: GenericFeedService

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var filterService: FilterService

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @Autowired
  lateinit var genericFeedDAO: GenericFeedDAO

  @Autowired
  lateinit var currentUser: CurrentUser

  @Throttled
  @DgsMutation
  suspend fun authAnonymous(dfe: DataFetchingEnvironment): AuthenticationDto = coroutineScope {
    val jwt = tokenProvider.createJwtForAnonymous()
    AuthenticationDto.newBuilder()
      .token(jwt.tokenValue)
      .corrId(CryptUtil.newCorrId())
      .build()
  }

  @Throttled
  @DgsMutation
  suspend fun authConfirmCode(@InputArgument data: ConfirmAuthCodeInput): Boolean = coroutineScope {
    mailAuthenticationService.confirmAuthCode(data)
    true
  }

//  @DgsMutation
//  @PreAuthorize("hasAuthority('WRITE')")
//  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
//  suspend fun createGenericFeed(
//    @InputArgument data: GenericFeedCreateInput,
//    @RequestHeader(ApiParams.corrId) corrId: String,
//  ): GenericFeed = coroutineScope {
//    toDTO(withContext(Dispatchers.IO) {
//      val user = currentUser.user()
//      resolve(corrId, data, user)
//    })!!
//  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateNativeFeed(
    @InputArgument data: NativeFeedUpdateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): NativeFeed = coroutineScope {
    toDTO(nativeFeedService.update(corrId, data.data, UUID.fromString(data.where.id)))
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createNativeFeed(
    @InputArgument data: NativeFeedCreateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): NativeFeed = coroutineScope {
    val nativeFeed = nativeFeedService.findByFeedUrl(data.feedUrl)
      .orElseGet {
        run {
          val fetchOptions = GenericFeedFetchOptions(
            prerender = false,
            websiteUrl = data.websiteUrl
          )
          val user = currentUser.user()
          val feed = feedDiscoveryService.discoverFeeds(corrId, fetchOptions).results.nativeFeeds.first()
          nativeFeedService.createNativeFeed(
            corrId,
            Optional.ofNullable(data.title).orElse(feed.title),
            Optional.ofNullable(feed.description).orElse("no description"),
            data.feedUrl,
            data.websiteUrl,
            BooleanUtils.isTrue(data.harvestItems),
            BooleanUtils.isTrue(data.harvestItems) && BooleanUtils.isTrue(data.harvestSiteWithPrerender),
            user
          )
        }
      }

    toDTO(nativeFeed)
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteNativeFeed(
    @InputArgument data: NativeFeedDeleteInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    nativeFeedService.delete(corrId, UUID.fromString(data.nativeFeed.id))
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun exportOpml(@RequestHeader(ApiParams.corrId) corrId: String): String = coroutineScope {

    ""
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun acceptTermsAndConditions(): Boolean = coroutineScope {
    userService.acceptTermsAndConditions()
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createImporters(
    @InputArgument("data") data: ImportersCreateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<Importer> = coroutineScope {
    val user = currentUser.user()
    resolve(corrId, data, user).map { toDTO(it) }
  }

  private fun resolve(corrId: String, data: ImportersCreateInput, user: UserEntity): List<ImporterEntity> {
    val bucket = resolve(corrId, data.bucket, user)
    return data.feeds.distinctBy { if (it.connect == null) { it.create.nativeFeed.feedUrl } else { it.connect.id } }
      .mapNotNull { runCatching { resolve(corrId, it, user) }.getOrNull() }
      .map { importerService.createImporter(corrId, it, bucket, data, user) }
  }

  private fun resolve(corrId: String, bucket: BucketCreateOrConnectInput, user: UserEntity): BucketEntity {
    return if (bucket.connect != null) {
      bucketService.findById(UUID.fromString(bucket.connect.id)).orElseThrow()
    } else if (bucket.create != null) {
      val data = bucket.create
      bucketService.createBucket(
        corrId,
        data.title,
        data.description,
        data.websiteUrl,
        fromDTO(data.visibility),
        user
      )
    } else {
      throw IllegalArgumentException()
    }
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteImporter(
    @InputArgument data: ImporterDeleteInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    importerService.delete(corrId, UUID.fromString(data.where.id))
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createBucket(
    @InputArgument data: BucketCreateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Bucket = coroutineScope {
    val user = currentUser.user()
    val bucket = bucketService.createBucket(
      corrId,
      title = data.title,
      description = data.description,
      websiteUrl = data.websiteUrl,
      visibility = fromDTO(data.visibility),
      user = user
    )

    toDTO(bucket)
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateBucket(
    @InputArgument data: BucketUpdateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Bucket = coroutineScope {
    val bucket = bucketService.updateBucket(
      corrId,
      data
    )
    toDTO(bucket)
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  suspend fun logout(dfe: DataFetchingEnvironment): Boolean = coroutineScope {
    val cookie = Cookie("TOKEN", "")
    cookie.isHttpOnly = true
    cookie.domain = propertyService.domain
    cookie.maxAge = 0
    ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!.addCookie(
      cookie
    )
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteBucket(
    @InputArgument data: BucketDeleteInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    bucketService.delete(corrId, UUID.fromString(data.where.id))
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createArticle(
    @InputArgument data: ArticleCreateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): ArticleEntity = coroutineScope {
    TODO()
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateArticles(
    @InputArgument data: ArticlesUpdateWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    articleService.updateAllByFilter(data.where, data.data)
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteArticles(
    @InputArgument data: ArticlesDeleteWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    articleService.deleteAllByFilter(data.where)
    true
  }

  fun resolve(corrId: String, data: GenericFeedCreateInput, user: UserEntity): GenericFeedEntity {
    val feedSpecification = GenericFeedUtil.fromDto(data.specification)

    val feedUrl = webToFeedTransformer.createFeedUrl(
      URL(data.websiteUrl),
      feedSpecification.selectors!!,
      feedSpecification.parserOptions,
      feedSpecification.fetchOptions,
      feedSpecification.refineOptions
    )

    val nativeFeed = nativeFeedService.createNativeFeed(
      corrId,
      data.title,
      data.description,
      feedUrl,
      data.websiteUrl,
      defaultsService.forHarvestItems(data.harvestItems),
      defaultsService.forHarvestItemsWithPrerender(data.harvestSiteWithPrerender),
      user
    )

    val genericFeed = GenericFeedEntity()
    genericFeed.websiteUrl = data.websiteUrl
    genericFeed.feedSpecification = feedSpecification
    genericFeed.nativeFeed = nativeFeed
    genericFeed.nativeFeedId = nativeFeed.id

    val saved = genericFeedDAO.save(genericFeed)
    log.debug("[${corrId}] created ${saved.id}")
    return saved
  }

  fun resolve(corrId: String, feed: NativeFeedCreateOrConnectInput, user: UserEntity): NativeFeedEntity {
    return if (feed.connect != null) {
      nativeFeedService.findById(UUID.fromString(feed.connect.id)).orElseThrow()
    } else {
      if (feed.create != null) {
        if (feed.create.nativeFeed != null) {
          val nativeData = feed.create.nativeFeed
          nativeFeedService.findByFeedUrl(nativeData.feedUrl) // todo and user
            .orElseGet {
              nativeFeedService.createNativeFeed(
                corrId,
                nativeData.title,
                nativeData.description,
                nativeData.feedUrl,
                nativeData.websiteUrl,
                defaultsService.forHarvestItems(nativeData.harvestItems),
                defaultsService.forHarvestItemsWithPrerender(nativeData.harvestSiteWithPrerender),
                user
              )
            }
        } else {
          val genericFeed = resolve(corrId, feed.create.genericFeed!!, user)
          genericFeed.nativeFeed!!
        }
      } else {
        throw IllegalArgumentException()
      }
    }
  }
}
