package org.migor.feedless

import com.netflix.graphql.dgs.subscriptions.websockets.DgsWebSocketAutoConfig
import org.springframework.boot.actuate.autoconfigure.data.elasticsearch.ElasticsearchReactiveHealthContributorAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticsearchRestHealthContributorAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy
@SpringBootApplication(
  exclude = [
    ReactiveElasticsearchRepositoriesAutoConfiguration::class,
    ElasticsearchRepositoriesAutoConfiguration::class,
    JpaRepositoriesAutoConfiguration::class,
    CacheAutoConfiguration::class,
    ElasticsearchRestHealthContributorAutoConfiguration::class,
    ElasticsearchReactiveHealthContributorAutoConfiguration::class,
    DgsWebSocketAutoConfig::class,
  ]
)
class RichRssApplication

fun main(args: Array<String>) {
  runApplication<RichRssApplication>(*args)
}
