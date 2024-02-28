package org.migor.feedless.api.auth

import reactor.core.publisher.FluxSink

open class InMemorySinkRepository<K, V> {
  private val sinkMap: MutableMap<K, FluxSink<V>> = mutableMapOf()
  fun pop(otp: K): FluxSink<V> {
    val sink = sinkMap[otp]
    remove(otp)
    return sink!!
  }

  fun store(otp: K, sink: FluxSink<V>) {
    sinkMap[otp] = sink
  }

  fun remove(otp: K) {
    sinkMap.remove(otp)
  }

  fun isEmpty(): Boolean {
    return sinkMap.isEmpty()
  }

}
