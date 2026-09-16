package org.migor.feedless.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class HarvestLogTest {

  @Test
  fun `a log line always carries millisecond precision`() {
    assertThat(harvestLogLine(LocalDateTime.of(2026, 9, 16, 14, 25, 54, 130_000_000), "a"))
      .isEqualTo("2026-09-16T14:25:54.130  a")
    assertThat(harvestLogLine(LocalDateTime.of(2026, 9, 16, 14, 26, 31, 152_281_782), "b"))
      .isEqualTo("2026-09-16T14:26:31.152  b")
  }

  @Test
  fun `an exception without message is described by its type`() {
    assertThat(IllegalArgumentException("").describe()).isEqualTo("IllegalArgumentException")
    assertThat(IllegalStateException().describe()).isEqualTo("IllegalStateException")
    assertThat(IllegalStateException("boom").describe()).isEqualTo("boom")
  }

  @Test
  fun `the import summary lists existing items, capped`() {
    assertThat(importSummary(retrieved = 3, took = 12, new = 3, existing = emptyList()))
      .isEqualTo("imported 3 items in 12ms: 3 new, 0 existing")
    assertThat(importSummary(retrieved = 7, took = 5, new = 1, existing = (1..6).map { "u$it" }))
      .isEqualTo("imported 7 items in 5ms: 1 new, 6 existing (u1, u2, u3, u4, u5, +1 more)")
  }
}
