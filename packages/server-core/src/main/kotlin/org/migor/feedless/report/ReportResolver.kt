package org.migor.feedless.report

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Report
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DgsComponent
@Profile("${AppProfiles.DEV_ONLY} & ${AppProfiles.report} & ${AppLayer.api}")
class ReportResolver(
  private val reportService: ReportService,
  private val sessionService: SessionService
) {

  private val log = LoggerFactory.getLogger(ReportResolver::class.simpleName)

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateReport)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createReport(
      dfe: DataFetchingEnvironment,
      @InputArgument repositoryId: String,
      @InputArgument segmentation: SegmentInput
  ): Report = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
      log.debug("createReport")
      reportService.createReport(repositoryId, segmentation, sessionService.userId()).toDto()
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteReport)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteReport(
      dfe: DataFetchingEnvironment,
      @InputArgument reportId: String,
  ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
      log.debug("deleteReport $reportId")
      reportService.deleteReport(reportId, sessionService.user())
      true
  }
}

private fun ReportEntity.toDto(): Report {
  return Report(
    id = id.toString(),
    createdAt = createdAt.toMillis(),
  )
}
