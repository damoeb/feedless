package org.migor.rich.rss.config

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableRabbit
@EnableScheduling
@Profile("rich")
class StatefullConfig {

}
