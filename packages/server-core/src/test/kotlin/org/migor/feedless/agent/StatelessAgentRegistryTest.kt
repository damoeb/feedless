package org.migor.feedless.agent

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime
import java.util.UUID

class StatelessAgentRegistryTest {

  private val registry = StatelessAgentRegistry()

  @Test
  fun `countConnected counts every registered agent, open or private, whoever owns it`() = runTest {
    registry.save(agent(owner = UserId(), openInstance = true))
    registry.save(agent(owner = UserId(), openInstance = false))

    assertThat(registry.countConnected()).isEqualTo(2)
  }

  @Test
  fun `countConnected drops a deleted agent`() = runTest {
    val kept = registry.save(agent(owner = UserId(), openInstance = true))
    val dropped = registry.save(agent(owner = UserId(), openInstance = true))

    registry.delete(dropped)

    assertThat(registry.countConnected()).isEqualTo(1)
    assertThat(registry.findByConnectionIdAndSecretKeyId(kept.connectionId, kept.secretKeyId!!)).isEqualTo(kept)
  }

  @Test
  fun `countConnected is 0 without agents`() = runTest {
    assertThat(registry.countConnected()).isEqualTo(0)
  }

  private fun agent(owner: UserId, openInstance: Boolean) = Agent(
    connectionId = UUID.randomUUID().toString(),
    version = "0.3.0",
    openInstance = openInstance,
    name = "agent",
    lastSyncedAt = LocalDateTime.now(),
    secretKeyId = UserSecretId(),
    ownerId = owner,
  )
}
