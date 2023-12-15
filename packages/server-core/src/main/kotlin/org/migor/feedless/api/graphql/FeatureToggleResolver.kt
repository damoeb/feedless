package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
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
import org.migor.feedless.generated.types.Agent
import org.migor.feedless.generated.types.Article
import org.migor.feedless.generated.types.ArticleWhereInput
import org.migor.feedless.generated.types.ArticlesInput
import org.migor.feedless.generated.types.ArticlesResponse
import org.migor.feedless.generated.types.Bucket
import org.migor.feedless.generated.types.BucketOrNativeFeed
import org.migor.feedless.generated.types.BucketWhereInput
import org.migor.feedless.generated.types.BucketsInput
import org.migor.feedless.generated.types.BucketsOrNativeFeedsInput
import org.migor.feedless.generated.types.BucketsResponse
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureName
import org.migor.feedless.generated.types.FeatureState
import org.migor.feedless.generated.types.GenericFeed
import org.migor.feedless.generated.types.GenericFeedWhereInput
import org.migor.feedless.generated.types.GenericFeedsInput
import org.migor.feedless.generated.types.GenericFeedsResponse
import org.migor.feedless.generated.types.ImportersInput
import org.migor.feedless.generated.types.ImportersResponse
import org.migor.feedless.generated.types.NativeFeed
import org.migor.feedless.generated.types.NativeFeedWhereInput
import org.migor.feedless.generated.types.NativeFeedsInput
import org.migor.feedless.generated.types.NativeFeedsResponse
import org.migor.feedless.generated.types.Plan
import org.migor.feedless.generated.types.Profile
import org.migor.feedless.generated.types.ServerSettings
import org.migor.feedless.generated.types.ServerSettingsContextInput
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.generated.types.WebDocumentWhereInput
import org.migor.feedless.service.AgentService
import org.migor.feedless.service.ArticleService
import org.migor.feedless.service.BucketOrNativeFeedService
import org.migor.feedless.service.BucketService
import org.migor.feedless.service.FeatureToggleService
import org.migor.feedless.service.FeedService
import org.migor.feedless.service.GenericFeedService
import org.migor.feedless.service.ImporterService
import org.migor.feedless.service.PlanService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.WebDocumentService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.env.Environment
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.context.request.ServletWebRequest
import java.util.*
import org.migor.feedless.generated.types.ApiUrls as ApiUrlsDto

@DgsComponent
@org.springframework.context.annotation.Profile(AppProfiles.database)
class FeatureToggleResolver {

  private val log = LoggerFactory.getLogger(FeatureToggleResolver::class.simpleName)

  @Autowired
  lateinit var currentUser: CurrentUser

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var featureToggleService: FeatureToggleService

  @DgsQuery
  @Cacheable(value = [CacheNames.GRAPHQL_RESPONSE], keyGenerator = "cacheKeyGenerator") // https://stackoverflow.com/questions/14072380/cacheable-key-on-multiple-method-arguments
  suspend fun serverSettings(
    @InputArgument data: ServerSettingsContextInput,
  ): ServerSettings = coroutineScope {
    log.info("serverSettings $data")
    val db = featureToggleService.withDatabase()
    val es = featureToggleService.withElasticSearch()
    ServerSettings.newBuilder()
      .apiUrls(
        ApiUrlsDto.newBuilder()
          .webToFeed("${propertyService.apiGatewayUrl}${ApiUrls.webToFeedFromRule}")
          .webToPageChange("${propertyService.apiGatewayUrl}${ApiUrls.webToFeedFromChange}")
          .build()
      )
      .features(mapOf(
        FeatureName.database to stable(db),
        FeatureName.puppeteer to stable(featureToggleService.withPuppeteer()),
        FeatureName.elasticsearch to experimental(es),
        FeatureName.genFeedFromFeed to stable(),
        FeatureName.genFeedFromPageChange to stable(),
        FeatureName.genFeedFromWebsite to stable(),
        FeatureName.authSSO to stable(propertyService.authentication == AppProfiles.authSSO),
        FeatureName.authMail to stable(propertyService.authentication == AppProfiles.authMail),
        FeatureName.authRoot to stable(propertyService.authentication == AppProfiles.authRoot),
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

  private fun beta(vararg requirements: Boolean): FeatureState {
    return if (requirements.isNotEmpty() && requirements.all { it }) {
      FeatureState.beta
    } else {
      FeatureState.off
    }
  }

}
