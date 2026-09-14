package org.migor.feedless.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReportRecipientTest {

  @Test
  fun `normalizes only case and surrounding spaces`() {
    assertThat(normalizeEmail("  Hans+News@Example.COM ")).isEqualTo("hans+news@example.com")
  }
}
