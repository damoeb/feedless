package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.config.CacheNames
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureName
import org.migor.feedless.generated.types.FeatureState
import org.migor.feedless.generated.types.ServerSettings
import org.migor.feedless.generated.types.ServerSettingsContextInput
import org.migor.feedless.service.FeatureService
import org.migor.feedless.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.env.Environment
import java.util.*
import org.migor.feedless.generated.types.ApiUrls as ApiUrlsDto

@DgsComponent
@org.springframework.context.annotation.Profile(AppProfiles.database)
class FeatureToggleResolver {

  private val log = LoggerFactory.getLogger(FeatureToggleResolver::class.simpleName)

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var featureService: FeatureService

  @DgsQuery
  @Cacheable(value = [CacheNames.GRAPHQL_RESPONSE], keyGenerator = "cacheKeyGenerator") // https://stackoverflow.com/questions/14072380/cacheable-key-on-multiple-method-arguments
  suspend fun serverSettings(
    @InputArgument data: ServerSettingsContextInput,
  ): ServerSettings = coroutineScope {
    log.info("serverSettings $data")
    val db = featureService.withDatabase()
    ServerSettings.newBuilder()
      .features(mapOf(
        FeatureName.database to stable(db),
        FeatureName.authSSO to stable(propertyService.authentication == AppProfiles.authSSO),
        FeatureName.authMail to stable(propertyService.authentication == AppProfiles.authMail),
//        FeatureName.authRoot to stable(propertyService.authentication == AppProfiles.authRoot),
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
