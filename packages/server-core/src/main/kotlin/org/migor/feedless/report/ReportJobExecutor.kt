package org.migor.feedless.report

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.report} & ${AppLayer.scheduler}")
class ReportJobExecutor internal constructor(
  val reportUseCase: ReportUseCase
) {

  private val log = LoggerFactory.getLogger(ReportJobExecutor::class.simpleName)

  @Scheduled(fixedDelay = 60000, initialDelay = 20000)
  fun sendScheduledReports() {
    runBlocking {
      coroutineScope {
        reportUseCase.processReportJobs()
      }
    }

  }
}
