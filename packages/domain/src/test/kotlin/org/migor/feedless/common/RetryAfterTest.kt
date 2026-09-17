package org.migor.feedless.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class RetryAfterTest {

  private val now = Instant.parse("2026-09-16T16:09:01Z")

  @Test
  fun `delta seconds`() {
    assertThat(RetryAfter.parse("600", now)).isEqualTo(Duration.ofSeconds(600))
  }

  @Test
  fun `http date`() {
    assertThat(RetryAfter.parse("Wed, 16 Sep 2026 16:19:01 GMT", now)).isEqualTo(Duration.ofMinutes(10))
  }

  @Test
  fun `missing, blank, garbage, zero, negative and past dates fall back`() {
    listOf(null, "", "soon", "0", "-5", "Wed, 16 Sep 2026 16:00:00 GMT").forEach {
      assertThat(RetryAfter.parse(it, now)).describedAs(it).isEqualTo(RetryAfter.FALLBACK)
    }
  }

  @Test
  fun `values above the cap are capped`() {
    assertThat(RetryAfter.parse("${Duration.ofDays(3).seconds}", now)).isEqualTo(RetryAfter.CAP)
  }

  @Test
  fun `fallback is 5 minutes and cap is 24 hours`() {
    assertThat(RetryAfter.FALLBACK).isEqualTo(Duration.ofMinutes(5))
    assertThat(RetryAfter.CAP).isEqualTo(Duration.ofHours(24))
  }
}
