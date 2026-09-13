package org.migor.feedless

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration

@Configuration
@SpringBootApplication(
  exclude = [
    DataJpaRepositoriesAutoConfiguration::class,
    CacheAutoConfiguration::class,
  ]
)
class FeedlessApplication

fun main(args: Array<String>) {
  runApplication<FeedlessApplication>(*args)
}
