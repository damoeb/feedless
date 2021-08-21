package org.migor.rss.rich.config

import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


object EventType {
  fun values(): Array<String> {
    return arrayOf(articleHarvested, readability, readabilityParsed, readabilityFailed)
  }

  const val articleHarvested = "articleHarvested";
  const val readability = "readability";
  const val readabilityParsed = "readabilityParsed";
  const val readabilityFailed = "readabilityFailed";
}

@Configuration
class RabbitMqConfig {

//  @Value("env.RABBITMQ_URL:amqp://localhost")
//  lateinit var rabbitUrl: String;

  @Bean
  fun template(): AmqpTemplate {
    val factory = CachingConnectionFactory("localhost")
    val admin: AmqpAdmin = RabbitAdmin(factory)
    EventType.values().forEach { eventType: String -> this.declareQueue(admin, eventType) }
    return RabbitTemplate(factory)
  }

  private fun declareQueue(admin: AmqpAdmin, eventType: String) {
    val q = Queue(eventType, true, false, false)
    admin.declareQueue(q)
  }
}
