package org.migor.rich.rss.config

import org.migor.rich.mq.generated.MqOperation
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

object RabbitQueue {
  fun values(): Array<String> {
    return arrayOf(articleChanged, askArticleScore, askPrerendering, prerenderingResult)
  }

  const val articleChanged = "MqArticleChanged"
  const val askArticleScore = "MqAskArticleScore"
  const val askPrerendering = "MqAskPrerendering"
  const val prerenderingResult = "MqPrerenderingResponse"
}

@Configuration
class RabbitMqConfig {

  private val log = LoggerFactory.getLogger(RabbitMqConfig::class.simpleName)

  @Autowired
  lateinit var factory: CachingConnectionFactory

  @Bean
  fun admin(): AmqpAdmin {
    val admin: AmqpAdmin = RabbitAdmin(factory)
    admin.initialize()
    return admin
  }

  @Bean
  fun template(@Autowired admin: AmqpAdmin): AmqpTemplate {
    RabbitQueue.values()
      .filter { op -> isSupportedMqOperation(op) }
      .forEach { queueName: String -> this.declareQueue(admin, queueName) }
    return RabbitTemplate(factory)
  }

  private fun isSupportedMqOperation(op: String): Boolean {
    val matchedOp = MqOperation.values().find { mqOperation -> op == mqOperation.name }
    return if (matchedOp == null) {
      this.log.warn("'$op' is not a supported operation")
      true
    } else {
      false
    }
  }

  private fun declareQueue(admin: AmqpAdmin, queueName: String) {
    this.log.info("Declaring queue $queueName")
    val q = Queue(queueName, true, false, false)
    admin.declareQueue(q)
  }
}
