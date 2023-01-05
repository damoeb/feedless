package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.migor.rich.rss.database.enums.BucketVisibility
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.ArticleCreateInputDto
import org.migor.rich.rss.generated.ArticleDeleteWhereInputDto
import org.migor.rich.rss.generated.ArticleUpdateWhereInputDto
import org.migor.rich.rss.generated.BucketCreateInputDto
import org.migor.rich.rss.generated.BucketDeleteInputDto
import org.migor.rich.rss.generated.BucketDto
import org.migor.rich.rss.generated.BucketVisibilityDto
import org.migor.rich.rss.generated.GenericFeedCreateInputDto
import org.migor.rich.rss.generated.GenericFeedDeleteInputDto
import org.migor.rich.rss.generated.GenericFeedDto
import org.migor.rich.rss.generated.ImporterCreateInputDto
import org.migor.rich.rss.generated.ImporterDeleteInputDto
import org.migor.rich.rss.generated.ImporterDto
import org.migor.rich.rss.generated.LoginResponseDto
import org.migor.rich.rss.generated.NativeFeedCreateInputDto
import org.migor.rich.rss.generated.NativeFeedDeleteInputDto
import org.migor.rich.rss.generated.NativeFeedDto
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.GenericFeedService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.service.NativeFeedService
import org.migor.rich.rss.service.UserService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
class MutationResolver {

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
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var userService: UserService

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun login(dfe: DataFetchingEnvironment): LoginResponseDto  = coroutineScope {
    val user = userService.getSystemUser()

    LoginResponseDto.builder()
      .setToken(authService.createTokenForUser(user))
      .setUser(toDTO(user))
      .build()
  }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
  suspend fun createGenericFeed(@InputArgument data: GenericFeedCreateInputDto,
                        dfe: DataFetchingEnvironment): GenericFeedDto = coroutineScope {
    toDTO(withContext(Dispatchers.IO) {
      genericFeedService.createGenericFeed(data)
    })!!
  }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteGenericFeed(@InputArgument data: GenericFeedDeleteInputDto,
                        dfe: DataFetchingEnvironment): Boolean = coroutineScope {
    genericFeedService.delete(UUID.fromString(data.genericFeed.id))
    true
  }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createNativeFeed(@InputArgument data: NativeFeedCreateInputDto,
                       dfe: DataFetchingEnvironment): NativeFeedDto = coroutineScope {
    val corrId = newCorrId()
    val nativeFeed = nativeFeedService.findByFeedUrl(data.feedUrl)
      .orElseGet {
        run {
          val feed = feedDiscoveryService.discoverFeeds(corrId, data.feedUrl).results.nativeFeeds.first()
          nativeFeedService.createNativeFeed(
            Optional.ofNullable(data.title).orElse(feed.title),
            Optional.ofNullable(feed.description).orElse("no description"),
            data.feedUrl,
            data.websiteUrl,
            BooleanUtils.isTrue(data.harvestSite),
            BooleanUtils.isTrue(data.harvestSiteWithPrerender)
          )
        }
      }

    toDTO(nativeFeed)
  }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteNativeFeed(@InputArgument data: NativeFeedDeleteInputDto,
                       dfe: DataFetchingEnvironment): Boolean = coroutineScope {
    nativeFeedService.delete(UUID.fromString(data.nativeFeed.id))
    true
  }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createImporter(@InputArgument("data") data: ImporterCreateInputDto,
                     dfe: DataFetchingEnvironment): ImporterDto = coroutineScope {
    toDTO(importerService.createImporter(data.feed, data.where.id, data.autoRelease))
  }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteImporter(@InputArgument data: ImporterDeleteInputDto,
                     dfe: DataFetchingEnvironment): Boolean = coroutineScope {
    importerService.delete(UUID.fromString(data.where.id))
    true
  }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createBucket(@InputArgument data: BucketCreateInputDto,
                   dfe: DataFetchingEnvironment): BucketDto = coroutineScope {
    val corrId = newCorrId()
    val user = userService.getSystemUser()
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
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteBucket(@InputArgument data: BucketDeleteInputDto,
                   dfe: DataFetchingEnvironment): Boolean = coroutineScope{
    bucketService.delete(UUID.fromString(data.where.id))
    true
  }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createArticle(@InputArgument data: ArticleCreateInputDto,
                   dfe: DataFetchingEnvironment): ArticleEntity = coroutineScope{
    TODO()
  }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateArticle(@InputArgument data: ArticleUpdateWhereInputDto,
                   dfe: DataFetchingEnvironment): ArticleEntity = coroutineScope{
    TODO()
  }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteArticle(@InputArgument data: ArticleDeleteWhereInputDto,
                   dfe: DataFetchingEnvironment): ArticleEntity = coroutineScope{
    TODO()
  }

  private fun toVisibility(visibility: BucketVisibilityDto): BucketVisibility {
    return when (visibility) {
      BucketVisibilityDto.isPublic -> BucketVisibility.public
      BucketVisibilityDto.isHidden -> BucketVisibility.hidden
      else -> throw IllegalArgumentException("visibility $visibility not supported")
    }
  }

}
