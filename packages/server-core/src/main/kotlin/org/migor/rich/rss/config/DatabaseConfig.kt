package org.migor.rich.rss.config

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableAutoConfiguration
@EnableRabbit
@EnableScheduling
@EnableJpaRepositories(value = ["org.migor.rich.rss.database.repository"])
@EntityScan(value = ["org.migor.rich.rss.database.model"])
@Profile("database")
class DatabaseConfig
