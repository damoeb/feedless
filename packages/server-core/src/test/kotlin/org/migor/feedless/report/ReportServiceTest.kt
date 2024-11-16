package org.migor.feedless.report

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReportServiceTest {

  @BeforeEach
  fun setUp() {
  }

  @Test
  fun `reports can be created by anonymous if repository is public`() {
    // todo test
  }

  @Test
  fun `reports can be created by owner if repository is private`() {
    // todo test
  }

  @Test
  fun `reports cannot be created by anonymous if repository is private`() {
    // todo test
  }

  @Test
  fun `report can be deleted by anonymous if created by anonymous`() {
    // todo test
  }

  @Test
  fun `report created by user, it can only be deleted by thee`() {
    // todo test
  }
}
