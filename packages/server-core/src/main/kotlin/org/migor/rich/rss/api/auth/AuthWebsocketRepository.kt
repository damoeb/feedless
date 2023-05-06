package org.migor.rich.rss.api.auth

import org.migor.rich.rss.data.jpa.models.OneTimePasswordEntity
import org.migor.rich.rss.generated.types.AuthenticationEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.FluxSink

@Service
class AuthWebsocketRepository: InMemorySinkRepository<String, AuthenticationEvent>() {
  fun pop(otp: OneTimePasswordEntity): FluxSink<AuthenticationEvent> {
    return super.pop(otp.id.toString())
  }

  fun store(otp: OneTimePasswordEntity, sink: FluxSink<AuthenticationEvent>) {
    super.store(otp.id.toString(), sink)
  }

  fun remove(otp: OneTimePasswordEntity) {
    super.remove(otp.id.toString())
  }

}
