package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.migor.rich.rss.api.ApiParams
import org.migor.rich.rss.data.jpa.enums.BucketVisibility
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.types.ArticleCreateInput
import org.migor.rich.rss.generated.types.ArticleDeleteWhereInput
import org.migor.rich.rss.generated.types.ArticleUpdateWhereInput
import org.migor.rich.rss.generated.types.Bucket
import org.migor.rich.rss.generated.types.BucketCreateInput
import org.migor.rich.rss.generated.types.BucketDeleteInput
import org.migor.rich.rss.generated.types.ConfirmAuthCodeInput
import org.migor.rich.rss.generated.types.GenericFeed
import org.migor.rich.rss.generated.types.GenericFeedCreateInput
import org.migor.rich.rss.generated.types.GenericFeedDeleteInput
import org.migor.rich.rss.generated.types.ImportOpmlInput
import org.migor.rich.rss.generated.types.Importer
import org.migor.rich.rss.generated.types.ImporterCreateInput
import org.migor.rich.rss.generated.types.ImporterDeleteInput
import org.migor.rich.rss.generated.types.NativeFeed
import org.migor.rich.rss.generated.types.NativeFeedCreateInput
import org.migor.rich.rss.generated.types.NativeFeedDeleteInput
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.http.Throttled
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.GenericFeedService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.service.NativeFeedService
import org.migor.rich.rss.service.UserService
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.security.Principal
import java.util.*
import org.migor.rich.rss.generated.types.Authentication as AuthenticationDto
import org.migor.rich.rss.generated.types.BucketVisibility as BucketVisibilityDto

@DgsComponent
class MutationResolver {

  private val log = LoggerFactory.getLogger(MutationResolver::class.simpleName)

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var feedDiscoveryService: FeedDiscoveryService

  @Autowired
  lateinit var nativeFeedService: NativeFeedService

  @Autowired
  lateinit var genericFeedService: GenericFeedService

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var userService: UserService

  @Throttled
  @DgsMutation
  suspend fun authAnonymous(dfe: DataFetchingEnvironment): AuthenticationDto = coroutineScope {
    authService.initiateAnonymousSession()
  }

  @Throttled
  @DgsMutation
  suspend fun authConfirmCode(@InputArgument data: ConfirmAuthCodeInput): Boolean = coroutineScope {
    authService.confirmAuthCode(data)
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
  suspend fun createGenericFeed(
    @InputArgument data: GenericFeedCreateInput,
    authentication: Authentication,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): GenericFeed = coroutineScope {
    toDTO(withContext(Dispatchers.IO) {
      genericFeedService.createGenericFeed(corrId, data)
    })!!
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteGenericFeed(
    @InputArgument data: GenericFeedDeleteInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    genericFeedService.delete(corrId, UUID.fromString(data.genericFeed.id))
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createNativeFeed(
    @InputArgument data: NativeFeedCreateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
    dfe: DataFetchingEnvironment
  ): NativeFeed = coroutineScope {
    val nativeFeed = nativeFeedService.findByFeedUrl(data.feedUrl)
      .orElseGet {
        run {
          val fetchOptions = GenericFeedFetchOptions(
            prerender = false,
            websiteUrl = data.websiteUrl
          )

          val feed = feedDiscoveryService.discoverFeeds(corrId, fetchOptions).results.nativeFeeds.first()
          nativeFeedService.createNativeFeed(
            corrId,
            Optional.ofNullable(data.title).orElse(feed.title),
            Optional.ofNullable(feed.description).orElse("no description"),
            data.feedUrl,
            data.websiteUrl,
            BooleanUtils.isTrue(data.autoRelease),
            BooleanUtils.isTrue(data.harvestItems),
            BooleanUtils.isTrue(data.harvestItems) && BooleanUtils.isTrue(data.harvestSiteWithPrerender)
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
  suspend fun importOpml(
    @InputArgument data: ImportOpmlInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
//    nativeFeedService.delete(UUID.fromString(data.nativeFeed.id))
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun exportOpml(@RequestHeader(ApiParams.corrId) corrId: String): String = coroutineScope {
//    nativeFeedService.delete(UUID.fromString(data.nativeFeed.id))
    ""
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createImporter(
    @InputArgument("data") data: ImporterCreateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Importer = coroutineScope {
    toDTO(importerService.createImporter(corrId, data.feed, data.where.id, data.autoRelease))
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
    principal: Principal,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Bucket = coroutineScope {
    val user = userService.findById(principal.name).get()
    val bucket = bucketService.createBucket(
      corrId,
      name = data.name,
      description = data.description,
      websiteUrl = data.websiteUrl,
      filter = data.filter,
      visibility = toVisibility(data.visibility),
      user = user
    )

    toDTO(bucket)
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
  suspend fun updateArticle(
    @InputArgument data: ArticleUpdateWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): ArticleEntity = coroutineScope {
    TODO()
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('WRITE')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteArticle(
    @InputArgument data: ArticleDeleteWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): ArticleEntity = coroutineScope {
    TODO()
  }

  private fun toVisibility(visibility: BucketVisibilityDto): BucketVisibility {
    return when (visibility) {
      BucketVisibilityDto.isPublic -> BucketVisibility.public
      BucketVisibilityDto.isHidden -> BucketVisibility.hidden
//      else -> throw IllegalArgumentException("visibility $visibility not supported")
    }
  }

}
