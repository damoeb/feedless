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
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.migor.feedless.generated.types.Report as ReportDto

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.DEV_ONLY} & ${AppProfiles.report} & ${AppLayer.api}")
class ReportResolver(
    private val reportService: ReportService,
    private val sessionService: SessionService
) {

    private val log = LoggerFactory.getLogger(ReportResolver::class.simpleName)

    @Throttled
    @DgsMutation(field = DgsConstants.MUTATION.CreateReport)
    suspend fun createReport(
        dfe: DataFetchingEnvironment,
        @InputArgument(DgsConstants.MUTATION.CREATEREPORT_INPUT_ARGUMENT.RepositoryId) repositoryId: String,
        @InputArgument(DgsConstants.MUTATION.CREATEREPORT_INPUT_ARGUMENT.Segmentation) segmentation: SegmentInput
    ): ReportDto = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
        log.debug("createReport")
        reportService.createReport(RepositoryId(repositoryId), segmentation, sessionService.userId()).toDto()
    }

    @Throttled
    @DgsMutation(field = DgsConstants.MUTATION.DeleteReport)
    suspend fun deleteReport(
        dfe: DataFetchingEnvironment,
        @InputArgument(DgsConstants.MUTATION.DELETEREPORT_INPUT_ARGUMENT.ReportId) reportId: String,
    ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
        log.debug("deleteReport $reportId")
        reportService.deleteReport(ReportId(reportId), sessionService.user())
        true
    }
}

internal fun Report.toDto(): ReportDto {
    return ReportDto(
        id = id.uuid.toString(),
        createdAt = createdAt.toMillis(),
    )
}
