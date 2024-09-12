package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.analytics.Tracked
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.BuildInfo
import org.migor.feedless.generated.types.ProfileName
import org.migor.feedless.generated.types.ServerSettings
import org.migor.feedless.generated.types.ServerSettingsContextInput
import org.migor.feedless.license.LicenseService
import org.migor.feedless.plan.ProductService
import org.migor.feedless.session.useRequestContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

@DgsComponent
@Profile("${AppProfiles.properties} & ${AppLayer.api}")
class ServerConfigResolver {

  private val log = LoggerFactory.getLogger(ServerConfigResolver::class.simpleName)

  @Value("\${APP_GIT_HASH}")
  lateinit var commit: String

  @Autowired
  private lateinit var environment: Environment

  @Autowired
  private lateinit var featureService: FeatureService

  @Value("\${APP_VERSION}")
  lateinit var version: String

  @Autowired
  private lateinit var productService: ProductService

  @Autowired
  private lateinit var licenseService: LicenseService

  @Tracked
  @DgsQuery
  @Cacheable(
    value = [CacheNames.SERVER_SETTINGS],
    keyGenerator = "cacheKeyGenerator"
  ) // https://stackoverflow.com/questions/14072380/cacheable-key-on-multiple-method-arguments
  suspend fun serverSettings(
    dfe: DataFetchingEnvironment,
    @InputArgument data: ServerSettingsContextInput,
  ): ServerSettings = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    log.debug("serverSettings $data")
    val product = data.product.fromDto()

    if (!licenseService.isTrial() && !licenseService.isLicenseNotNeeded() && !licenseService.isLicensedForProduct(
        product
      )
    ) {
      throw IllegalArgumentException("license does not support product ${product.name}")
    }

    ServerSettings(
      appUrl = productService.getAppUrl(product),
      version = version,
      build = BuildInfo(
        date = licenseService.buildFrom(),
        commit = commit
      ),
      profiles = environment.activeProfiles.map {
        when (it) {
          AppProfiles.mail -> ProfileName.authMail
          AppProfiles.authSSO -> ProfileName.authSSO
          AppProfiles.selfHosted -> ProfileName.selfHosted
          AppProfiles.dev -> ProfileName.dev
          else -> null
        }
      }.filterNotNull(),
      gatewayUrl = productService.getGatewayUrl(product),
      features = featureService.findAllByProduct(product),
    )
  }
}
