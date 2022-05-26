package org.migor.rich.rss.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.devtools.autoconfigure.DevToolsDataSourceAutoConfiguration
import org.springframework.boot.devtools.autoconfigure.DevToolsR2dbcAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@EnableAutoConfiguration(exclude = [
  DataSourceAutoConfiguration::class,
  DataSourceTransactionManagerAutoConfiguration::class,
  HibernateJpaAutoConfiguration::class,
  RabbitAutoConfiguration::class,
  DevToolsDataSourceAutoConfiguration::class,
  DevToolsR2dbcAutoConfiguration::class
])
@Profile("stateless")
class StatelessConfig
