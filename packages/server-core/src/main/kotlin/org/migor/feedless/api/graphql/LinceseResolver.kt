package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.generated.types.License
import org.migor.feedless.generated.types.LicenseData
import org.migor.feedless.generated.types.UpdateLicenseInput
import org.migor.feedless.service.LicenseService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@org.springframework.context.annotation.Profile(AppProfiles.database)
class LinceseResolver {

  private val log = LoggerFactory.getLogger(LinceseResolver::class.simpleName)

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var propertyService: PropertyService

  @Value("\${APP_VERSION}")
  lateinit var version: String

  @Autowired
  lateinit var licenseService: LicenseService

  @DgsQuery
  suspend fun license(): License = coroutineScope {
    getLicense()
  }

  @DgsMutation
  suspend fun updateLicense(
    @RequestHeader(ApiParams.corrId, required = false) corrIdParam: String,
    dfe: DataFetchingEnvironment,
    @InputArgument data: UpdateLicenseInput,
  ): License = coroutineScope {
    val corrId = CryptUtil.handleCorrId(corrIdParam)
    log.info("[$corrId] updateLicense")

    licenseService.updateLicense(data.licenseRaw)
    getLicense()
  }

  private fun getLicense(): License {
    val payload = licenseService.getLicensePayload()
    return License.newBuilder()
      .isValid(licenseService.hasValidLicenseOrLicenseNotNeeded())
      .isTrial(licenseService.isTrial())
      .isLocated(payload != null)
      .trialUntil(licenseService.getTrialUntil())
      .data(payload?.let {
        LicenseData.newBuilder()
          .name(payload.name)
          .email(payload.email)
          .type(payload.type)
          .date(payload.date)
          .build()
      })
      .build()
  }
}
