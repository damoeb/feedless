package org.migor.feedless.browserautomation

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime
import java.util.UUID

class StatelessBrowserAutomationRegistryTest {

  private val registry = StatelessBrowserAutomationRegistry()

  @Test
  fun `countConnected counts every registered agent, open or private, whoever owns it`() = runTest {
    registry.save(browserAutomation(owner = UserId(), openInstance = true))
    registry.save(browserAutomation(owner = UserId(), openInstance = false))

    assertThat(registry.countConnected()).isEqualTo(2)
  }

  @Test
  fun `countConnected drops a deleted agent`() = runTest {
    val kept = registry.save(browserAutomation(owner = UserId(), openInstance = true))
    val dropped = registry.save(browserAutomation(owner = UserId(), openInstance = true))

    registry.delete(dropped)

    assertThat(registry.countConnected()).isEqualTo(1)
    assertThat(registry.findByConnectionIdAndSecretKeyId(kept.connectionId, kept.secretKeyId!!)).isEqualTo(kept)
  }

  @Test
  fun `countConnected is 0 without agents`() = runTest {
    assertThat(registry.countConnected()).isEqualTo(0)
  }

  @Test
  fun `findAllByOwnerIdOrOpenInstanceIsTrue lists the owner's agents and every open one`() = runTest {
    val alice = UserId()
    val bob = UserId()
    val aliceOpen = registry.save(browserAutomation(owner = alice, openInstance = true))
    val alicePrivate = registry.save(browserAutomation(owner = alice, openInstance = false))
    val bobOpen = registry.save(browserAutomation(owner = bob, openInstance = true))
    registry.save(browserAutomation(owner = bob, openInstance = false))

    assertThat(registry.findAllByOwnerIdOrOpenInstanceIsTrue(alice))
      .containsExactlyInAnyOrder(aliceOpen, alicePrivate, bobOpen)
  }

  @Test
  fun `findAllByOwnerIdOrOpenInstanceIsTrue lists only open agents without a user`() = runTest {
    val aliceOpen = registry.save(browserAutomation(owner = UserId(), openInstance = true))
    registry.save(browserAutomation(owner = UserId(), openInstance = false))

    assertThat(registry.findAllByOwnerIdOrOpenInstanceIsTrue(null)).containsExactly(aliceOpen)
  }

  private fun browserAutomation(owner: UserId, openInstance: Boolean) = BrowserAutomation(
    connectionId = UUID.randomUUID().toString(),
    version = "0.3.0",
    openInstance = openInstance,
    name = "agent",
    lastSyncedAt = LocalDateTime.now(),
    secretKeyId = UserSecretId(),
    ownerId = owner,
  )
}
