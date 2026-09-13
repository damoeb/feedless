package org.migor.feedless.browserautomation

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.SecurityContextCapabilityService
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.security.core.context.SecurityContextHolder
import org.migor.feedless.generated.types.Agent as AgentDto
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime
import java.time.ZoneOffset

class BrowserAutomationResolverTest {

    @Test
    fun `agents for an anonymous caller lists the open instances`() = runTest {
        val previousContext = SecurityContextHolder.getContext()
        SecurityContextHolder.clearContext()
        try {
            val openAgent = BrowserAutomation(
                connectionId = "connectionId",
                version = "version",
                openInstance = true,
                name = "open",
                lastSyncedAt = LocalDateTime.parse("2020-01-02T09:00:00"),
                secretKeyId = UserSecretId(),
                ownerId = UserId(),
                createdAt = LocalDateTime.parse("2020-01-02T10:15:30"),
            )
            val directory = mock<BrowserAutomationDirectory> {
                onBlocking { findAllByOwnerIdOrOpenInstanceIsTrue(null) } doReturn listOf(openAgent)
            }
            val resolver = BrowserAutomationResolver(mock(), directory, SecurityContextCapabilityService())

            val actual = resolver.agents(mock())

            assertThat(actual.map { it.name }).containsExactly("open")
        } finally {
            SecurityContextHolder.setContext(previousContext)
        }
    }

    @Test
    fun testDto() {
        val createdAt = LocalDateTime.parse("2020-01-02T10:15:30")
        val secretKeyId = UserSecretId()
        val ownerId = UserId()

        val incoming = org.migor.feedless.browserautomation.BrowserAutomation(
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
