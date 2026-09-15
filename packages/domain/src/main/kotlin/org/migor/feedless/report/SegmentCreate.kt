package org.migor.feedless.report

import org.migor.feedless.geo.LatLonPoint
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class SegmentCreate(
  val recipientEmail: String,
  val recipientName: String,
  val startingAt: LocalDateTime,
  val interval: ChronoUnit,
  val near: LatLonPoint? = null,
  val nearDistanceKm: Double? = null,
  val reporterPluginId: String,
)
