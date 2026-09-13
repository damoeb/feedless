package org.migor.feedless.browserautomation

import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime
import java.util.*

data class BrowserAutomation(
    val id: BrowserAutomationId = BrowserAutomationId(UUID.randomUUID()),
    val connectionId: String,
    val version: String,
    val openInstance: Boolean,
    val name: String,
    val lastSyncedAt: LocalDateTime,
    val secretKeyId: UserSecretId? = null,
    val ownerId: UserId,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

