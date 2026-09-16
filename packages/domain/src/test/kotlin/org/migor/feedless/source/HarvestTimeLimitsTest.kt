package org.migor.feedless.source

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class HarvestTimeLimitsTest {

  private val now = LocalDateTime.of(2026, 9, 16, 12, 0)
  private val planFloor = now.plusMinutes(30)
  private val cronCap = now.plusHours(2)

  @Test
  fun `requested before the floor is coerced up to the floor`() {
    val requested = now.minusHours(1)

    val actual = HarvestTimeLimits.coerce(requested, now, planFloor, null, cronCap)

    assertThat(actual).isEqualTo(planFloor)
  }

  @Test
  fun `requested after the cap is coerced down to the cap`() {
    val requested = cronCap.plusHours(1)

    val actual = HarvestTimeLimits.coerce(requested, now, planFloor, null, cronCap)

    assertThat(actual).isEqualTo(cronCap)
  }

  @Test
  fun `a floor after the cap wins over the cap`() {
    val lateFloor = cronCap.plusHours(1)
    val requested = cronCap.plusHours(2)

    val actual = HarvestTimeLimits.coerce(requested, now, lateFloor, null, cronCap)

    assertThat(actual).isEqualTo(lateFloor)
  }

  @Test
  fun `never harvested falls back to now as the plan floor`() {
    val requested = now.minusDays(1)

    val actual = HarvestTimeLimits.coerce(requested, now, null, null, cronCap)

    assertThat(actual).isEqualTo(now)
  }

  @Test
  fun `a blank cron applies no cap`() {
    val requested = now.plusYears(1)

    val actual = HarvestTimeLimits.coerce(requested, now, planFloor, null, null)

    assertThat(actual).isEqualTo(requested)
  }

  @Test
  fun `an active cooldown later than the plan floor wins`() {
    val hostBlockedUntil = planFloor.plusHours(1)
    val requested = now

    val actual = HarvestTimeLimits.coerce(requested, now, planFloor, hostBlockedUntil, cronCap)

    assertThat(actual).isEqualTo(hostBlockedUntil)
  }

  @Test
  fun `requested inside the floor and cap is unchanged`() {
    val requested = now.plusHours(1)

    val actual = HarvestTimeLimits.coerce(requested, now, planFloor, null, cronCap)

    assertThat(actual).isEqualTo(requested)
  }
}
