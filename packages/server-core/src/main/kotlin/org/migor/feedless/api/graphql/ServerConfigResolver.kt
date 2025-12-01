package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.api.fromDto
import org.migor.feedless.config.CacheNames
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AuthType
import org.migor.feedless.generated.types.BuildInfo
import org.migor.feedless.generated.types.ProfileName
import org.migor.feedless.generated.types.ServerSettings
import org.migor.feedless.generated.types.ServerSettingsContextInput
import org.migor.feedless.license.LicenseService
import org.migor.feedless.session.ProductAuthProperties
import org.migor.feedless.session.ProductsAuthProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.properties} & ${AppLayer.api}")
class ServerConfigResolver {

  private val log = LoggerFactory.getLogger(ServerConfigResolver::class.simpleName)

  @Value("\${APP_GIT_COMMIT}")
  private lateinit var commit: String

  @Autowired
  private lateinit var environment: Environment

  @Value("\${app.version}")
  private lateinit var version: String

  @Autowired
  private lateinit var licenseService: LicenseService

  @Autowired
  private lateinit var analyticsService: AnalyticsService

  @Autowired
  private lateinit var productsAuthProperties: ProductsAuthProperties

  @DgsQuery(field = DgsConstants.QUERY.ServerSettings)
  @Cacheable(
    value = [CacheNames.SERVER_SETTINGS],
    keyGenerator = "cacheKeyGenerator"
  ) // https://stackoverflow.com/questions/14072380/cacheable-key-on-multiple-method-arguments
  suspend fun serverSettings(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.SERVERSETTINGS_INPUT_ARGUMENT.Data) data: ServerSettingsContextInput,
  ): ServerSettings = coroutineScope {
    log.debug("serverSettings $data")
    analyticsService.track()
    val product = data.product.fromDto()

    if (!licenseService.isTrial() && !licenseService.isLicenseNotNeeded() && !licenseService.isLicensedForProduct(
        product
      )
    ) {
      throw IllegalArgumentException("license does not support product ${product.name}")
    }

    ServerSettings(
      version = version,
      build = BuildInfo(
        date = licenseService.getBuildDate(),
        commit = commit
      ),
      auth = getProductAuthProperties(product),
      profiles = environment.activeProfiles.map {
        when (it) {
          AppProfiles.selfHosted -> ProfileName.selfHosted
          AppProfiles.DEV_ONLY -> ProfileName.dev
          else -> null
        }
      }.filterNotNull(),
    )
  }

  private fun getProductAuthProperties(product: Vertical): List<AuthType> {
    val props = when (product) {
      Vertical.rssProxy -> productsAuthProperties.rssProxy
      Vertical.feedless -> productsAuthProperties.feedless
      Vertical.untoldNotes -> productsAuthProperties.untold
      Vertical.upcoming -> productsAuthProperties.upcoming
      Vertical.visualDiff -> productsAuthProperties.visualDiff
      else -> ProductAuthProperties()
    }

    val resolve: (Boolean, AuthType) -> AuthType? = { enabled, authType ->
      if (enabled) {
        authType
      } else {
        null
      }
    }

    return listOf(
      resolve(props.oauth, AuthType.oauth),
      resolve(props.mailToken, AuthType.mailToken),
    ).filterNotNull()
  }
}
