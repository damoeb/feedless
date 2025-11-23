package org.migor.feedless.data.jpa.connectedApp

import org.migor.feedless.connectedApp.ConnectedApp

fun ConnectedAppEntity.toDomain(): ConnectedApp {
    return when (this) {
        is TelegramConnectionEntity -> this.toDomain()
        is GithubConnectionEntity -> this.toDomain()
        else -> throw IllegalArgumentException("Unknown ConnectedAppEntity type: ${this::class.simpleName}")
    }
}
