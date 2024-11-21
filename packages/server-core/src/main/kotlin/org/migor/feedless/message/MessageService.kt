package org.migor.feedless.message

import org.migor.feedless.feed.parser.json.JsonItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@Service
@Transactional(propagation = Propagation.NEVER)
class MessageService {

  private val topics: MutableMap<String, Sinks.Many<JsonItem>> = mutableMapOf()


  private fun forTopic(topic: String): Sinks.Many<JsonItem> {
    return if (topics.containsKey(topic)) {
      topics[topic]!!
    } else {
      val sink = Sinks.many().multicast().onBackpressureBuffer<JsonItem>()
      topics[topic] = sink
      sink
    }
  }

  fun publishMessage(topic: String, item: JsonItem) {
    forTopic(topic).tryEmitNext(item)
  }

  fun subscribe(topic: String): Flux<JsonItem> {
    return forTopic(topic).asFlux()
  }
}
