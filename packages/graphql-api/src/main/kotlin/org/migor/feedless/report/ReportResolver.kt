package org.migor.feedless.report

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.IntervalUnit
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.session.injectCapabilitiesFromSecurityContext
import org.migor.feedless.util.toLocalDateTime
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import java.time.temporal.ChronoUnit
import org.migor.feedless.generated.types.Report as ReportDto

@DgsComponent
@Profile("${AppProfiles.DEV_ONLY} & ${AppProfiles.report} & ${AppLayer.api}")
class ReportResolver(
  private val reportUseCase: ReportUseCase,
) {

  private val log = LoggerFactory.getLogger(ReportResolver::class.simpleName)

  @Throttled
  @PreAuthorize("@capabilityService.hasCapability('user')")
  @DgsMutation(field = DgsConstants.MUTATION.CreateReport)
  suspend fun createReport(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.CREATEREPORT_INPUT_ARGUMENT.RepositoryId) repositoryId: String,
    @InputArgument(DgsConstants.MUTATION.CREATEREPORT_INPUT_ARGUMENT.Segmentation) segmentation: SegmentInput
  ): ReportDto = withContext(context = injectCapabilitiesFromSecurityContext()) {
    log.debug("createReport")
    reportUseCase.createReport(RepositoryId(repositoryId), segmentation.toDomain()).toDto()
  }

  @Throttled
  @PreAuthorize("@capabilityService.hasCapability('user')")
  @DgsMutation(field = DgsConstants.MUTATION.DeleteReport)
  suspend fun deleteReport(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.DELETEREPORT_INPUT_ARGUMENT.ReportId) reportId: String,
  ): Boolean = withContext(context = injectCapabilitiesFromSecurityContext()) {
    log.debug("deleteReport $reportId")
    reportUseCase.deleteReport(ReportId(reportId))
    true
  }
}

internal fun Report.toDto(): ReportDto {
  return ReportDto(
    id = id.uuid.toString(),
    createdAt = createdAt.toMillis(),
  )
}

private fun SegmentInput.toDomain() = SegmentCreate(
  recipientEmail = recipient.email.email,
  recipientName = recipient.email.name,
  startingAt = `when`.scheduled.startingAt.toLocalDateTime(),
  interval = when (`when`.scheduled.interval) {
    IntervalUnit.MONTH -> ChronoUnit.MONTHS
    IntervalUnit.WEEK -> ChronoUnit.WEEKS
  },
  near = what.latLng?.near?.let { LatLonPoint(it.point.lat, it.point.lng) },
  nearDistanceKm = what.latLng?.near?.distanceKm,
  reporterPluginId = report.plugin.pluginId,
)
