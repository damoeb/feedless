package org.migor.rich.rss.config

import org.migor.rich.rss.AppProfiles
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@Profile(AppProfiles.database)
@Configuration
@EnableAutoConfiguration(exclude = [
  ElasticsearchDataAutoConfiguration::class,
  ReactiveElasticsearchRestClientAutoConfiguration::class,
  ReactiveElasticsearchRepositoriesAutoConfiguration::class,
  ElasticsearchRepositoriesAutoConfiguration::class
])
@EnableRabbit
@EnableScheduling
@EnableTransactionManagement
@EntityScan(value = ["org.migor.rich.rss.database.models"])
@EnableJpaRepositories(value = ["org.migor.rich.rss.database.repositories"])
@EnableJpaAuditing
class DatabaseConfig
