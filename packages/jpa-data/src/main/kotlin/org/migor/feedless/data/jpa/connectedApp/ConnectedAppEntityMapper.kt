package org.migor.feedless.data.jpa.connectedApp

import org.migor.feedless.connectedApp.ConnectedApp
import org.migor.feedless.connectedApp.GithubConnection
import org.migor.feedless.connectedApp.TelegramConnection

fun ConnectedAppEntity.toDomain(): ConnectedApp {
  return when (this) {
    is TelegramConnectionEntity -> this.toDomain()
    is GithubConnectionEntity -> this.toDomain()
    else -> throw IllegalArgumentException("Unknown ConnectedAppEntity type: ${this::class.simpleName}")
  }
}

fun ConnectedApp.toEntity(): ConnectedAppEntity {
  return when (this) {
    is TelegramConnection -> this.toEntity()
    is GithubConnection -> this.toEntity()
//    else -> throw IllegalArgumentException("Unknown ConnectedApp type: ${this::class.simpleName}")
  }
}
