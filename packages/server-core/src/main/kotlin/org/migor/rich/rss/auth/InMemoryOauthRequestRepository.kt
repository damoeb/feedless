package org.migor.rich.rss.auth

import org.migor.rich.rss.data.jpa.models.OneTimePasswordEntity
import org.migor.rich.rss.generated.types.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.FluxSink

@Service
class InMemoryOauthRequestRepository {
  private val otpId2sink: MutableMap<String, FluxSink<Authentication>> = mutableMapOf()
  fun pop(otp: OneTimePasswordEntity): FluxSink<Authentication>? {
    val sink = otpId2sink[otp.id.toString()]
    remove(otp)
    return sink
  }

  fun store(otp: OneTimePasswordEntity, sink: FluxSink<Authentication>) {
    otpId2sink[otp.id.toString()] = sink
  }

  fun remove(otp: OneTimePasswordEntity) {
    otpId2sink.remove(otp.id.toString())
  }

}
