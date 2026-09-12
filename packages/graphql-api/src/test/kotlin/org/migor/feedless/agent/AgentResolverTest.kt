package org.migor.feedless.agent

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.generated.types.Agent as AgentDto
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime
import java.time.ZoneOffset

class AgentResolverTest {

    @Test
    fun testDto() {
        val createdAt = LocalDateTime.parse("2020-01-02T10:15:30")
        val secretKeyId = UserSecretId()
        val ownerId = UserId()

        val incoming = org.migor.feedless.agent.Agent(
            connectionId = "connectionId",
            version = "version",
            openInstance = false,
            name = "name",
            lastSyncedAt = LocalDateTime.parse("2020-01-02T09:00:00"),
            secretKeyId = secretKeyId,
            ownerId = ownerId,
            createdAt = createdAt,
        )

        val expected = AgentDto(
            ownerId = ownerId.toString(),
            name = "name",
            addedAt = createdAt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
            version = "version",
            openInstance = false,
            secretKeyId = secretKeyId.toString(),
        )

        assertThat(incoming.toDto()).isEqualTo(expected)
    }
}
