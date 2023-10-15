package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import jakarta.servlet.http.Cookie
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.Throttled
import org.migor.feedless.api.WebToPageChangeParams
import org.migor.feedless.api.auth.CookieProvider
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.api.auth.MailAuthenticationService
import org.migor.feedless.api.auth.TokenProvider
import org.migor.feedless.api.graphql.DtoResolver.fromDTO
import org.migor.feedless.api.graphql.DtoResolver.toDTO
import org.migor.feedless.data.jpa.models.ArticleEntity
import org.migor.feedless.data.jpa.models.BucketEntity
import org.migor.feedless.data.jpa.models.GenericFeedEntity
import org.migor.feedless.data.jpa.models.ImporterEntity
import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.repositories.GenericFeedDAO
import org.migor.feedless.generated.types.ArticleCreateInput
import org.migor.feedless.generated.types.ArticlesDeleteWhereInput
import org.migor.feedless.generated.types.ArticlesUpdateWhereInput
import org.migor.feedless.generated.types.AuthUserInput
import org.migor.feedless.generated.types.Bucket
import org.migor.feedless.generated.types.BucketCreateOrConnectInput
import org.migor.feedless.generated.types.BucketDeleteInput
import org.migor.feedless.generated.types.BucketUpdateInput
import org.migor.feedless.generated.types.BucketsCreateInput
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.CreateNativeFeedsInput
import org.migor.feedless.generated.types.DeleteApiTokensInput
import org.migor.feedless.generated.types.FragmentWatchCreateInput
import org.migor.feedless.generated.types.GenericFeedCreateInput
import org.migor.feedless.generated.types.Importer
import org.migor.feedless.generated.types.ImporterAttributesInput
import org.migor.feedless.generated.types.ImporterDeleteInput
import org.migor.feedless.generated.types.ImporterUpdateInput
import org.migor.feedless.generated.types.ImportersCreateInput
import org.migor.feedless.generated.types.NativeFeed
import org.migor.feedless.generated.types.NativeFeedCreateInput
import org.migor.feedless.generated.types.NativeFeedCreateOrConnectInput
import org.migor.feedless.generated.types.NativeFeedDeleteInput
import org.migor.feedless.generated.types.NativeFeedUpdateInput
import org.migor.feedless.generated.types.NativeGenericOrFragmentFeedCreateInput
import org.migor.feedless.generated.types.SubmitAgentDataInput
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.generated.types.UserSecret
import org.migor.feedless.service.AgentService
import org.migor.feedless.service.ArticleService
import org.migor.feedless.service.BucketService
import org.migor.feedless.service.ImporterService
import org.migor.feedless.service.NativeFeedService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.StatefulUserSecretService
import org.migor.feedless.service.UserService
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.CryptUtil.handleCorrId
import org.migor.feedless.util.GenericFeedUtil
import org.migor.feedless.web.WebToFeedTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.context.request.ServletWebRequest
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import org.migor.feedless.generated.types.Authentication as AuthenticationDto

@DgsComponent
@Profile(AppProfiles.database)
class MutationResolver {

  private val log = LoggerFactory.getLogger(MutationResolver::class.simpleName)

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var mailAuthenticationService: MailAuthenticationService

  @Autowired
  lateinit var agentService: AgentService

  @Autowired
  lateinit var nativeFeedService: NativeFeedService

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var cookieProvider: CookieProvider

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @Autowired
  lateinit var genericFeedDAO: GenericFeedDAO

  @Autowired
  lateinit var userSecretService: StatefulUserSecretService

  @Autowired
  lateinit var currentUser: CurrentUser

  @Throttled
  @DgsMutation
  suspend fun authAnonymous(@RequestHeader(ApiParams.corrId, required = false) corrIdParam: String,
                            dfe: DataFetchingEnvironment,
  ): AuthenticationDto = coroutineScope {
    val corrId = handleCorrId(corrIdParam)
    log.info("[$corrId] authAnonymous")
    val jwt = tokenProvider.createJwtForAnonymous()
    addCookie(dfe, cookieProvider.createTokenCookie(jwt))
    AuthenticationDto.newBuilder()
      .token(jwt.tokenValue)
      .corrId(CryptUtil.newCorrId())
      .build()
  }

  private fun addCookie(dfe: DataFetchingEnvironment, cookie: Cookie) {
    ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!.addCookie(
      cookie
    )
  }

  @Throttled
  @DgsMutation
  suspend fun authUser(@RequestHeader(ApiParams.corrId, required = false) corrIdParam: String,
                       dfe: DataFetchingEnvironment,
                       @InputArgument data: AuthUserInput,
  ): AuthenticationDto = coroutineScope {
    val corrId = handleCorrId(corrIdParam)
    log.info("[$corrId] authRoot")
    if (propertyService.authentication == AppProfiles.authRoot) {
      val root = userService.findByEmail(data.email) ?: throw IllegalArgumentException("user not found")
      if (!root.isRoot) {
        throw IllegalAccessException("account is not root")
      }
      userSecretService.findBySecretKeyValue(data.secretKey, data.email)
        ?: throw IllegalArgumentException("secretKey does not match")
      val jwt = tokenProvider.createJwtForUser(root)
      addCookie(dfe, cookieProvider.createTokenCookie(jwt))
      AuthenticationDto.newBuilder()
        .token(jwt.tokenValue)
        .corrId(CryptUtil.newCorrId())
        .build()
    } else {
      throw java.lang.IllegalArgumentException("authRoot profile is not active")
    }
  }

  @Throttled
  @DgsMutation
  suspend fun authConfirmCode(
    @InputArgument data: ConfirmAuthCodeInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    log.info("[$corrId] authConfirmCode")
    mailAuthenticationService.confirmAuthCode(data)
    true
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('PROVIDE_HTTP_RESPONSE')")
  suspend fun submitAgentData(@InputArgument data: SubmitAgentDataInput): Boolean = coroutineScope {
    log.info("[${data.corrId}] submitAgentData")
    agentService.handleScrapeResponse(data.corrId, data.jobId, data.scrapeResponse)
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
    log.info("[$corrId] updateNativeFeed")
    toDTO(nativeFeedService.update(corrId, data.data, UUID.fromString(data.where.id)))
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createApiToken(
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): UserSecret = coroutineScope {
    toDTO(userSecretService.createApiToken(corrId, currentUser.user()), false)
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteApiTokens(
    @InputArgument data: DeleteApiTokensInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    userSecretService.deleteApiTokens(corrId, currentUser.user(), data.where.`in`.map { UUID.fromString(it) })
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createNativeFeeds(
    @InputArgument data: CreateNativeFeedsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<NativeFeed> = coroutineScope {
    log.info("[$corrId] createNativeFeeds")
    data.feeds.map { toDTO(resolve(corrId, it, currentUser.user())) }
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteNativeFeed(
    @InputArgument data: NativeFeedDeleteInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    log.info("[$corrId] deleteNativeFeed ${data.nativeFeed.id}")
    nativeFeedService.delete(corrId, UUID.fromString(data.nativeFeed.id))
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateCurrentUser(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: UpdateCurrentUserInput,
  ): Boolean = coroutineScope {
    log.info("[$corrId] updateCurrentUser ${currentUser.userId()} $data")
    userService.updateUser(corrId, currentUser.userId()!!, data)
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createImporters(
    @InputArgument("data") data: ImportersCreateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<Importer> = coroutineScope {
    log.info("[$corrId] createImporters $data")
    val user = currentUser.user()
    resolve(corrId, data, user).map { toDTO(it) }
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateImporter(
    @InputArgument("data") data: ImporterUpdateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Importer = coroutineScope {
    log.info("[$corrId] updateImporter $data")
    toDTO(
      importerService.update(corrId, data)
    )
  }

  private fun resolve(corrId: String, data: NativeGenericOrFragmentFeedCreateInput, user: UserEntity): NativeFeedEntity {
    return data.nativeFeed?.let {
      resolve(corrId, it, user)
    } ?: data.genericFeed?.let {
      resolve(corrId, it, user)
    } ?: data.fragmentFeed?.let {
      resolve(corrId, it, user)
    }!!
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteImporter(
    @InputArgument data: ImporterDeleteInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    log.info("[$corrId] deleteImporter ${data.where.id}")
    importerService.delete(corrId, UUID.fromString(data.where.id))
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createBuckets(
    @InputArgument data: BucketsCreateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<Bucket> = coroutineScope {
    log.info("[$corrId] createBuckets $data")
    val user = currentUser.user()
    data.buckets.map {
      run {
        val bucket = bucketService.createBucket(
          corrId,
          title = it.title,
          description = it.description,
          websiteUrl = it.websiteUrl,
          visibility = fromDTO(it.visibility),
          user = user,
          tags = it.tags
        )
        it.importers?.let {it.map { importer -> resolve(corrId, bucket, importer.feeds, user, importer.protoImporter) }}
        bucket
      }
    }.map { toDTO(it) }
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateBucket(
    @InputArgument data: BucketUpdateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Bucket = coroutineScope {
    log.info("[$corrId] updateBucket ${data.where.id}")
    val bucket = bucketService.updateBucket(
      corrId,
      data
    )
    toDTO(bucket)
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  suspend fun logout(dfe: DataFetchingEnvironment,
                     @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    log.info("[$corrId] logout")
    val cookie = Cookie("TOKEN", "")
    cookie.isHttpOnly = true
    cookie.domain = propertyService.domain
    cookie.maxAge = 0
    addCookie(dfe, cookie)
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteBucket(
    @InputArgument data: BucketDeleteInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    log.info("[$corrId] deleteBucket ${data.where.id}")
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
    log.info("[$corrId] updateArticles")
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
    log.info("[$corrId] deleteArticles ${data.where}")
    articleService.deleteAllByFilter(data.where)
    true
  }

  private fun resolve(corrId: String, data: ImportersCreateInput, user: UserEntity): List<ImporterEntity> {
    val bucket = resolve(corrId, data.bucket, user)
    return resolve(corrId, bucket, data.feeds, user, data.protoImporter)
  }

  private fun resolve(corrId: String, bucket: BucketEntity, feeds: List<NativeFeedCreateOrConnectInput>, user: UserEntity, importer: ImporterAttributesInput): List<ImporterEntity> {
    // todo generic feed should use a hash
    return feeds.distinctBy { if (it.connect == null) { it.create.nativeFeed?.feedUrl ?: it.create.genericFeed.specification.selectors.contextXPath } else { it.connect.id } }
      .map { resolve(corrId, it, user) }
      .map { importerService.createImporter(corrId, it, bucket, importer, user) }
  }

  private fun resolve(corrId: String, bucket: BucketCreateOrConnectInput, user: UserEntity): BucketEntity {
    return if (bucket.connect != null) {
      bucketService.findById(UUID.fromString(bucket.connect.id))
        .orElseThrow { IllegalArgumentException("bucket not found") }
    } else if (bucket.create != null) {
      val data = bucket.create
      bucketService.createBucket(
        corrId,
        data.title,
        data.description,
        data.websiteUrl,
        fromDTO(data.visibility),
        user,
        data.tags
      )
    } else {
      throw IllegalArgumentException("connect or create expected")
    }
  }

  fun resolve(corrId: String, data: FragmentWatchCreateInput, user: UserEntity): NativeFeedEntity {
    val encode: (value: String) -> String = { value -> URLEncoder.encode(value, StandardCharsets.UTF_8) }
    val params: List<Pair<String, String>> = mapOf(
      WebToPageChangeParams.url to data.scrapeOptions.page.url,
      WebToPageChangeParams.version to "0.1",
      WebToPageChangeParams.xpath to data.fragmentXpath,
      WebToPageChangeParams.prerender to "${ data.scrapeOptions.page.prerender != null }",
      WebToPageChangeParams.prerenderWaitUntil to data.scrapeOptions.page.prerender?.waitUntil,
      WebToPageChangeParams.type to data.compareBy,
      WebToPageChangeParams.format to "atom",
    ).map { entry -> entry.key to encode("${entry.value}") }

    val searchParams = params.fold("") { acc, pair -> acc + "${pair.first}=${pair.second}&" }
    val feedUrl = "${propertyService.apiGatewayUrl}${ApiUrls.webToFeedFromChange}?$searchParams"
    return nativeFeedService.createNativeFeed(
      corrId,
      data.title,
      "page change feed",
      feedUrl,
      data.scrapeOptions.page.url,
      emptyList(),
      user,
      retentionSize = 2
    )

  }

  fun resolve(corrId: String, data: GenericFeedCreateInput, user: UserEntity): NativeFeedEntity {
    val feedSpecification = GenericFeedUtil.fromDto(data.specification)

    val websiteUrl = feedSpecification.scrapeOptions.page.url
    val feedUrl = webToFeedTransformer.createFeedUrl(
      URL(websiteUrl),
      feedSpecification.selectors!!,
      feedSpecification.parserOptions,
      feedSpecification.scrapeOptions,
      feedSpecification.refineOptions
    )

    val genericFeed = GenericFeedEntity()
    genericFeed.websiteUrl = websiteUrl
    genericFeed.feedSpecification = feedSpecification
//    genericFeed.nativeFeed = nativeFeed
//    genericFeed.nativeFeedId = nativeFeed.id

    return nativeFeedService.createNativeFeed(
      corrId,
      data.title,
      data.description,
      feedUrl,
      websiteUrl,
      emptyList(),
      user,
      genericFeedDAO.save(genericFeed)
    )
  }

  fun resolve(corrId: String, feed: NativeFeedCreateOrConnectInput, user: UserEntity): NativeFeedEntity {
    return if (feed.connect != null) {
      nativeFeedService.findById(UUID.fromString(feed.connect.id))
        .orElseThrow { IllegalArgumentException("nativeFeed not found") }
    } else {
      if (feed.create != null) {
        if (feed.create.nativeFeed != null) {
          val nativeData = feed.create.nativeFeed
          resolve(corrId, nativeData, user)
        } else {
          resolve(corrId, feed.create.genericFeed!!, user)
        }
      } else {
        throw IllegalArgumentException("Either connect or create must be specified")
      }
    }
  }

  private fun resolve(corrId: String, nativeData: NativeFeedCreateInput, user: UserEntity): NativeFeedEntity {
    return nativeFeedService.findByFeedUrlAndOwnerId(nativeData.feedUrl, user.id)
      .orElseGet {
        nativeFeedService.createNativeFeed(
          corrId,
          nativeData.title,
          nativeData.description,
          nativeData.feedUrl,
          nativeData.websiteUrl,
          nativeData.plugins,
          user
        )
      }
  }
}
