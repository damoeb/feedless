package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.data.jpa.models.toDto
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureBooleanValue
import org.migor.feedless.generated.types.FeatureName
import org.migor.feedless.generated.types.FeatureValue
import org.migor.feedless.generated.types.ServerSettings
import org.migor.feedless.generated.types.ServerSettingsContextInput
import org.migor.feedless.service.FeatureService
import org.migor.feedless.service.ProductService
import org.migor.feedless.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.env.Environment
import java.util.*

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

  @Autowired
  lateinit var productService: ProductService

  @DgsQuery
  @Cacheable(
    value = [CacheNames.GRAPHQL_RESPONSE],
    keyGenerator = "cacheKeyGenerator"
  ) // https://stackoverflow.com/questions/14072380/cacheable-key-on-multiple-method-arguments
  suspend fun serverSettings(
    @InputArgument data: ServerSettingsContextInput,
  ): ServerSettings = coroutineScope {
    log.info("serverSettings $data")
    val db = featureService.withDatabase()
    val features = mapOf(
      FeatureName.database to db,
      FeatureName.authSSO to (propertyService.authentication == AppProfiles.authSSO),
      FeatureName.authMail to (propertyService.authentication == AppProfiles.authMail),
//        FeatureName.authRoot to stable(propertyService.authentication == AppProfiles.authRoot),
    ).map {
      Feature.newBuilder()
        .name(it.key)
        .value(
          FeatureValue.newBuilder()
            .boolVal(
              FeatureBooleanValue.newBuilder()
                .value(true)
                .build()
            )
            .build()
        )
        .build()
    }

    val product = data.product.fromDto()
    ServerSettings.newBuilder()
      .appUrl(productService.getAppUrl(product))
      .gatewayUrl(productService.getGatewayUrl(product))
      .features(features.plus(featureService.findAllByProduct(product).map { it.toDto() }))
      .build()
  }

}
