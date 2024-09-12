package org.migor.feedless.config

import com.netflix.graphql.dgs.context.DgsCustomContextBuilder
import org.migor.feedless.AppLayer
import org.migor.feedless.common.CacheKeyGenerator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Configuration
@Profile(AppLayer.api)
class GraphqlConfig {

  @Bean
  @Qualifier("cacheKeyGenerator")
  fun cacheKeyGenerator(): CacheKeyGenerator {
    return CacheKeyGenerator()
  }

  @Component
  class CustomContextBuilder : DgsCustomContextBuilder<CustomContext> {
    override fun build(): CustomContext {
      return CustomContext()
    }
  }
}

class CustomContext {
  var repositoryId: String? = null
  var userId: String? = null
}
