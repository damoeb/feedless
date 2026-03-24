package org.migor.feedless.pipeline

import org.migor.feedless.report.Report

interface SinkPlugin : Plugin {

  suspend fun report(
    report: Report,
  )
}
