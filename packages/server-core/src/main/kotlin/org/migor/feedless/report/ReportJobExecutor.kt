package org.migor.feedless.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.DEV_ONLY} & ${AppProfiles.report} & ${AppLayer.scheduler}")
class ReportJobExecutor internal constructor(
  val reportRepository: ReportRepository,
  val reportUseCase: ReportUseCase
) {

  private val log = LoggerFactory.getLogger(ReportJobExecutor::class.simpleName)

  //  @Scheduled(fixedDelay = 60000, initialDelay = 20000)
  fun askForReportAuthorization() {

  }
}
