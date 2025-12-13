//package org.migor.feedless.session
//
//import org.migor.feedless.AppLayer
//import org.migor.feedless.AppProfiles
//import org.migor.feedless.common.InMemorySinkRepository
//import org.migor.feedless.generated.types.AuthenticationEvent
//import org.migor.feedless.mail.OneTimePasswordEntity
//import org.springframework.context.annotation.Profile
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Propagation
//import org.springframework.transaction.annotation.Transactional
//import reactor.core.publisher.FluxSink
//
//@Service
//@Profile("${AppProfiles.session} & ${AppLayer.repository}")
//class AuthWebsocketRepository : InMemorySinkRepository<String, AuthenticationEvent>() {
//  fun pop(otp: OneTimePasswordEntity): FluxSink<AuthenticationEvent> {
//    return super.pop(otp.id.toString())
//  }
//
//  fun store(otp: OneTimePasswordEntity, sink: FluxSink<AuthenticationEvent>) {
//    super.store(otp.id.toString(), sink)
//  }
//
//  fun remove(otp: OneTimePasswordEntity) {
//    super.remove(otp.id.toString())
//  }
//
//}
