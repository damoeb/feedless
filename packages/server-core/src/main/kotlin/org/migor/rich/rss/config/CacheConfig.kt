package org.migor.rich.rss.config

import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import org.ehcache.event.EventType
import org.ehcache.jsr107.Eh107Configuration
import org.migor.rich.rss.cache.CacheEventLogger
import org.migor.rich.rss.generated.types.ServerSettings
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.jcache.JCacheCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import javax.cache.Caching


object CacheNames {
  const val HTTP_RESPONSE = "httpResponseCache"
  const val GRAPHQL_RESPONSE = "graphqlResponseCache"
}

@Configuration
@EnableCaching
//@Profile("!${AppProfiles.nocache}")
class CacheConfig {

  @Bean
  fun jCacheCacheManager(): JCacheCacheManager {
    return JCacheCacheManager(cacheManager())
  }

  @Bean(destroyMethod = "close")
  fun cacheManager(): javax.cache.CacheManager {
    val provider = Caching.getCachingProvider()
    val cacheManager = provider.cacheManager

    val asynchronousListener = CacheEventListenerConfigurationBuilder
      .newEventListenerConfiguration(
        CacheEventLogger(),
        EventType.CREATED,
        EventType.EXPIRED
      )
      .unordered()
      .asynchronous()

    val httpResponseCache = CacheConfigurationBuilder.newCacheConfigurationBuilder(
      String::class.java,
      org.migor.rich.rss.service.HttpResponse::class.java,
      ResourcePoolsBuilder.heap(1000)
        .offheap(25, MemoryUnit.MB)
    )
      .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(4)))

    val graphqlResponseCache = CacheConfigurationBuilder.newCacheConfigurationBuilder(
      String::class.java,
      ServerSettings::class.java,
      ResourcePoolsBuilder.heap(10)
    )
      .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10)))


    cacheManager.createCache(
      CacheNames.HTTP_RESPONSE,
      Eh107Configuration.fromEhcacheCacheConfiguration(httpResponseCache.withService(asynchronousListener))
    )

    cacheManager.createCache(
      CacheNames.GRAPHQL_RESPONSE,
      Eh107Configuration.fromEhcacheCacheConfiguration(graphqlResponseCache.withService(asynchronousListener))
    )

//    cacheManager.createCache("")

    return cacheManager
  }
}
