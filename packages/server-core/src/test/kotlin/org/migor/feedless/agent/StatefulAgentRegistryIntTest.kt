package org.migor.feedless.agent

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.data.jpa.agent.AgentDAO
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretRepository
import org.migor.feedless.userSecret.UserSecretType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test",
  "database",
  AppProfiles.agent,
  AppProfiles.user,
  AppProfiles.secrets,
  AppLayer.repository,
)
@MockitoBean(
  types = [
    StatelessAuthService::class,
  ]
)
@Testcontainers
class StatefulAgentRegistryIntTest {

  @Autowired
  private lateinit var agentRepository: AgentRepository

  @Autowired
  private lateinit var agentDAO: AgentDAO

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var userSecretRepository: UserSecretRepository

  private lateinit var registry: StatefulAgentRegistry

  @BeforeEach
  fun setUp() {
    // t_agent is shared with every other test on this container: count from an empty table.
    agentDAO.deleteAll()
    registry = StatefulAgentRegistry(agentRepository)
  }

  @Test
  fun `countConnected counts every agent row, open or private, whoever owns it`() = runBlocking<Unit> {
    val alice = newUser()
    val bob = newUser()
    registry.save(agent(owner = alice.id, secret = secretOf(alice.id), openInstance = true))
    registry.save(agent(owner = alice.id, secret = secretOf(alice.id), openInstance = false))
    registry.save(agent(owner = bob.id, secret = secretOf(bob.id), openInstance = false))

    assertThat(registry.countConnected()).isEqualTo(3)
  }

  @Test
  fun `countConnected drops an agent once its row is deleted`() = runBlocking<Unit> {
    val alice = newUser()
    val kept = registry.save(agent(owner = alice.id, secret = secretOf(alice.id), openInstance = true))
    val dropped = registry.save(agent(owner = alice.id, secret = secretOf(alice.id), openInstance = true))

    registry.delete(dropped)

    assertThat(registry.countConnected()).isEqualTo(1)
    assertThat(registry.findByConnectionIdAndSecretKeyId(kept.connectionId, kept.secretKeyId!!)).isNotNull()
  }

  @Test
  fun `countConnected is 0 without agents`() = runBlocking<Unit> {
    assertThat(registry.countConnected()).isEqualTo(0)
  }

  private fun newUser(): User = userRepository.save(
    User(email = "agent-registry-${UUID.randomUUID()}@feedless.test", lastLogin = LocalDateTime.now()),
  )

  private fun secretOf(owner: UserId): UserSecretId = userSecretRepository.save(
    UserSecret(
      value = UUID.randomUUID().toString(),
      validUntil = LocalDateTime.now().plusDays(1),
      type = UserSecretType.SecretKey,
      ownerId = owner,
    ),
  ).id

  private fun agent(owner: UserId, secret: UserSecretId, openInstance: Boolean) = Agent(
    connectionId = UUID.randomUUID().toString(),
    version = "0.3.0",
    openInstance = openInstance,
    name = "agent",
    lastSyncedAt = LocalDateTime.now(),
    secretKeyId = secret,
    ownerId = owner,
  )
}
