package org.migor.feedless.config

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.CacheKeyGenerator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(AppLayer.api)
class GraphqlConfig {

  @Bean
  @Qualifier("cacheKeyGenerator")
  fun cacheKeyGenerator(): CacheKeyGenerator {
    return CacheKeyGenerator()
  }
}
