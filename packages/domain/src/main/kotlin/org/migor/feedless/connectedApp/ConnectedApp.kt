package org.migor.feedless.connectedApp

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

sealed class ConnectedApp(
    open val id: ConnectedAppId = ConnectedAppId(),
    open val authorized: Boolean,
    open val authorizedAt: LocalDateTime? = null,
    open val userId: UserId?,
    open val createdAt: LocalDateTime = LocalDateTime.now(),
)

