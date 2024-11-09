package org.migor.feedless

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class ExceptionsTest {
  @Test
  fun `TemporaryServerException is resumable`() {
    assertTrue(mock(TemporaryServerException::class.java) is ResumableHarvestException)
  }
  @Test
  fun `PermissionDeniedException is fatal`() {
    assertTrue(mock(PermissionDeniedException::class.java) is FatalHarvestException)
  }
  @Test
  fun `BadRequestException is fatal`() {
    assertTrue(mock(BadRequestException::class.java) is FatalHarvestException)
  }
  @Test
  fun `UnavailableException is resumable`() {
    assertTrue(mock(UnavailableException::class.java) is ResumableHarvestException)
  }
  @Test
  fun `SiteNotFoundException is fatal`() {
    assertTrue(mock(SiteNotFoundException::class.java) is FatalHarvestException)
  }
  @Test
  fun `HostOverloadingException is resumable`() {
    assertTrue(mock(HostOverloadingException::class.java) is ResumableHarvestException)
  }
}
