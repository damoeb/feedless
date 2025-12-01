package org.migor.feedless.license

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.toDto
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.LocalizedLicense
import org.migor.feedless.generated.types.UpdateLicenseInput
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.license} & ${AppLayer.api}")
class LinceseResolver {

  private val log = LoggerFactory.getLogger(LinceseResolver::class.simpleName)

  @Value("\${app.version}")
  lateinit var version: String

  @Autowired
  private lateinit var licenseService: LicenseService

  @DgsMutation(field = DgsConstants.MUTATION.UpdateLicense)
  suspend fun updateLicense(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.UPDATELICENSE_INPUT_ARGUMENT.Data) data: UpdateLicenseInput,
  ): LocalizedLicense = coroutineScope {
    log.debug("updateLicense")

    licenseService.updateLicense(data.licenseRaw)
    getLicense()
  }

  @DgsData(parentType = DgsConstants.SERVERSETTINGS.TYPE_NAME, field = DgsConstants.SERVERSETTINGS.License)
  suspend fun license(dfe: DgsDataFetchingEnvironment): LocalizedLicense = coroutineScope {
    getLicense()
  }


  private fun getLicense(): LocalizedLicense {
    val payload = licenseService.getLicensePayload()
    return LocalizedLicense(
      isValid = licenseService.hasValidLicenseOrLicenseNotNeeded(),
      isTrial = licenseService.isTrial(),
      isLocated = payload != null,
      trialUntil = licenseService.getTrialUntil(),
      data = payload?.let {
        org.migor.feedless.generated.types.License(
          name = payload.name,
          email = payload.email,
          createdAt = payload.createdAt.toMillis(),
          scope = payload.scope.toDto(),
          version = payload.version,
        )
      }
    )
  }
}
