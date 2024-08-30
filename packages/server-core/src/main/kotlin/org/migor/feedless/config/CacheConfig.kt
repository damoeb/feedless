package org.migor.feedless.config

import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import org.ehcache.event.EventType
import org.ehcache.jsr107.Eh107Configuration
import org.migor.feedless.AppProfiles
import org.migor.feedless.agent.AgentResponse
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.generated.types.ServerSettings
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.JsonUtil
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.cache.jcache.JCacheCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.lang.reflect.Method
import java.time.Duration
import javax.cache.Caching


object CacheNames {
  const val FEED_LONG_TTL = "feedResponseCache10min"
  const val FEED_SHORT_TTL = "feedResponseCache2Min"
  const val HTTP_RESPONSE = "httpResponseCache"
  const val AGENT_RESPONSE = "agentResponseCache"
  const val SERVER_SETTINGS = "graphqlResponseCache"
}

class AgentResponseCacheKeyGenerator : KeyGenerator {
  override fun generate(target: Any, method: Method, vararg params: Any?): Any {
    return CryptUtil.sha1(JsonUtil.gson.toJson(params[1]))
  }

}

@Configuration
@EnableCaching
@Profile("${AppProfiles.cache} & !${AppProfiles.dev}")
class CacheConfig {

  @Bean
  fun jCacheCacheManager(): JCacheCacheManager {
    return JCacheCacheManager(cacheManager())
  }

  @Bean("agentResponseCacheKeyGenerator")
  fun keyGenerator(): KeyGenerator {
    return AgentResponseCacheKeyGenerator()
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


    cacheManager.createCache(
      CacheNames.HTTP_RESPONSE,
      Eh107Configuration.fromEhcacheCacheConfiguration(
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
          String::class.java,
          HttpResponse::class.java,
          ResourcePoolsBuilder.heap(1000)
            .offheap(100, MemoryUnit.MB)
        )
          .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10)))
          .withService(asynchronousListener)
      )
    )
    cacheManager.createCache(
      CacheNames.AGENT_RESPONSE,
      Eh107Configuration.fromEhcacheCacheConfiguration(
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
          String::class.java,
          AgentResponse::class.java,
          ResourcePoolsBuilder.heap(1000)
            .offheap(50, MemoryUnit.MB)
        )
          .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(4)))
          .withService(asynchronousListener)
      )
    )
    cacheManager.createCache(
      CacheNames.FEED_LONG_TTL,
      Eh107Configuration.fromEhcacheCacheConfiguration(
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
          String::class.java,
          JsonFeed::class.java,
          ResourcePoolsBuilder.heap(20000)
            .offheap(1000, MemoryUnit.MB)
        )
          .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10)))
          .withService(asynchronousListener)
      )
    )

    cacheManager.createCache(
      CacheNames.FEED_SHORT_TTL,
      Eh107Configuration.fromEhcacheCacheConfiguration(
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
          String::class.java,
          JsonFeed::class.java,
          ResourcePoolsBuilder.heap(10000)
            .offheap(300, MemoryUnit.MB)
        )
          .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(2)))
          .withService(asynchronousListener)
      )
    )

    cacheManager.createCache(
      CacheNames.SERVER_SETTINGS,
      Eh107Configuration.fromEhcacheCacheConfiguration(
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
          String::class.java,
          ServerSettings::class.java,
          ResourcePoolsBuilder.heap(10)
        )
          .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(120)))
          .withService(asynchronousListener)
      )
    )

    return cacheManager
  }
}
