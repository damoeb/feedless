package org.migor.feedless.report

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.repository.RepositoryId
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * The part of the lifecycle verifiable without a database: how the stored
 * Segmentation becomes the selection description a future recommendation
 * profile will hook into.
 */
class ReportLifecycleTest {

  private val now = LocalDateTime.of(2026, 9, 10, 8, 0)

  private fun segmentation(
    interval: ChronoUnit = ChronoUnit.WEEKS,
    latLon: LatLonPoint? = null,
    distance: Double? = null,
  ) = Segmentation(
    size = 200,
    timeSegmentStartingAt = now,
    timeInterval = interval,
    repositoryId = RepositoryId(),
    contentSegmentLatLon = latLon,
    contentSegmentLatLonDistance = distance,
  )

  @Test
  fun `a weekly segmentation looks one week ahead`() = runTest {
    val spec = segmentation().toSpec(now)

    assertThat(spec.from).isEqualTo(now)
    assertThat(spec.until).isEqualTo(now.plusWeeks(1))
  }

  /**
   * The report announces what's coming, rather than reporting what happened
   * - so the window starts now and reaches forward.
   */
  @Test
  fun `the window never reaches into the past`() = runTest {
    val spec = segmentation().toSpec(now)

    assertThat(spec.from).isAfterOrEqualTo(now)
    assertThat(spec.until).isAfter(spec.from)
  }

  @Test
  fun `the interval unit drives the window length`() = runTest {
    assertThat(segmentation(ChronoUnit.DAYS).toSpec(now).until).isEqualTo(now.plusDays(1))
    assertThat(segmentation(ChronoUnit.MONTHS).toSpec(now).until).isEqualTo(now.plusMonths(1))
  }

  @Test
  fun `carries the geo filter when both point and distance are set`() = runTest {
    val spec = segmentation(latLon = LatLonPoint(47.1679898, 8.5173652), distance = 10.0).toSpec(now)

    assertThat(spec.near).isNotNull
    assertThat(spec.near!!.lat).isEqualTo(47.1679898)
    assertThat(spec.near!!.lng).isEqualTo(8.5173652)
    assertThat(spec.near!!.distanceKm).isEqualTo(10.0)
  }

  @Test
  fun `drops the geo filter when the distance is missing`() = runTest {
    val spec = segmentation(latLon = LatLonPoint(47.0, 8.0), distance = null).toSpec(now)

    assertThat(spec.near).isNull()
  }

  @Test
  fun `passes the size through as the page size`() = runTest {
    assertThat(segmentation().toSpec(now).maxSize).isEqualTo(200)
  }
}
