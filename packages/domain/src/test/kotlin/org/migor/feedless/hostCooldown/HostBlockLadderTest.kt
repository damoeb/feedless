package org.migor.feedless.hostCooldown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class HostBlockLadderTest {

  @Test
  fun `escalates per strike and settles at 24 hours`() {
    assertThat((1..7).map { HostBlockLadder.delayFor(it) }).containsExactly(
      Duration.ofMinutes(5),
      Duration.ofMinutes(30),
      Duration.ofHours(2),
      Duration.ofHours(12),
      Duration.ofHours(24),
      Duration.ofHours(24),
      Duration.ofHours(24),
    )
  }

  @Test
  fun `strike zero or below is treated as the first strike`() {
    assertThat(HostBlockLadder.delayFor(0)).isEqualTo(Duration.ofMinutes(5))
  }
}
