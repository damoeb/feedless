package org.migor.feedless.config

import org.migor.feedless.AppLayer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler

@Configuration
@Profile(AppLayer.security)
class MethodSecurityConfig {

  @Bean
  fun methodSecurityExpressionHandler(): MethodSecurityExpressionHandler {
    return CustomMethodSecurityExpressionHandler()
  }
}


