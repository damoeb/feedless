package org.migor.feedless.license

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.data.jpa.enums.toDto
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.License
import org.migor.feedless.generated.types.LocalizedLicense
import org.migor.feedless.generated.types.UpdateLicenseInput
import org.migor.feedless.session.useRequestContext
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader

@DgsComponent
@Profile("${AppProfiles.database} & ${AppProfiles.api}")
class LinceseResolver {

  private val log = LoggerFactory.getLogger(LinceseResolver::class.simpleName)

  @Value("\${APP_VERSION}")
  lateinit var version: String

  @Autowired
  private lateinit var licenseService: LicenseService

  @DgsMutation(field = DgsConstants.MUTATION.UpdateLicense)
  suspend fun updateLicense(
    @RequestHeader(ApiParams.corrId, required = false) corrIdParam: String,
    dfe: DataFetchingEnvironment,
    @InputArgument data: UpdateLicenseInput,
  ): LocalizedLicense = withContext(useRequestContext(currentCoroutineContext())) {
    val corrId = CryptUtil.handleCorrId(corrIdParam)
    log.debug("[$corrId] updateLicense")

    licenseService.updateLicense(corrId, data.licenseRaw)
    getLicense()
  }

  @DgsData(parentType = DgsConstants.SERVERSETTINGS.TYPE_NAME, field = DgsConstants.SERVERSETTINGS.License)
  @Transactional
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
        License(
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
